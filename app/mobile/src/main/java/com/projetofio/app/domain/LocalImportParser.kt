package com.projetofio.app.domain

import java.nio.ByteBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId

enum class ImportIssueCode {
    FILE_TOO_LARGE,
    TOO_MANY_LINES,
    TOO_MANY_ENTRIES,
    ENTRY_TOO_LARGE,
    MALFORMED_UTF8,
    UNSUPPORTED_CONTAINER,
    ACTIVE_CONTENT,
    CONTROL_CONTENT,
    UNSUPPORTED_STRUCTURE,
    MISSING_DATE,
    INVALID_DATE,
    INVALID_SIZE,
    TIME_LIMIT,
}

data class ImportIssue(val code: ImportIssueCode, val sourceIndex: Int? = null)

data class ImportCandidate(
    val sourceIndex: Int,
    val originalCreatedAt: Instant,
    val originalTimeZone: String,
    val content: String,
    val fingerprint: String,
)

data class ParsedImport(
    val source: ImportSource,
    val candidates: List<ImportCandidate>,
    val issues: List<ImportIssue>,
    val parserVersion: String = LocalImportParser.VERSION,
) {
    val canCommit: Boolean get() = candidates.isNotEmpty() && issues.isEmpty()
}

fun interface MonotonicNanos { fun now(): Long }

class LocalImportParser(
    private val nanos: MonotonicNanos = MonotonicNanos(System::nanoTime),
) {
    fun parse(bytes: ByteArray, source: ImportSource): ParsedImport {
        val started = nanos.now()
        if (bytes.size > MAX_FILE_BYTES) return failed(source, ImportIssueCode.FILE_TOO_LARGE)
        if (looksLikeContainer(bytes)) return failed(source, ImportIssueCode.UNSUPPORTED_CONTAINER)
        val text = decode(bytes) ?: return failed(source, ImportIssueCode.MALFORMED_UTF8)
        if (text.count { it == '\n' } + 1 > MAX_LINES) return failed(source, ImportIssueCode.TOO_MANY_LINES)
        if ('\u0000' in text || text.count(::unsupportedControl) > MAX_CONTROL_CHARS) {
            return failed(source, ImportIssueCode.CONTROL_CONTENT)
        }
        if (ACTIVE_CONTENT.containsMatchIn(text)) return failed(source, ImportIssueCode.ACTIVE_CONTENT)
        if (
            (text.contains(TEXT_START) && occurrences(text, TEXT_START) != occurrences(text, TEXT_END)) ||
            (text.contains(GENERIC_START) && occurrences(text, GENERIC_START) != occurrences(text, GENERIC_END))
        ) return failed(source, ImportIssueCode.UNSUPPORTED_STRUCTURE)

        val blocks = when {
            text.contains(TEXT_START) -> parseTextBlocks(text)
            text.startsWith("# Fio — exportação local") && text.contains("Formato: fio-export-v1") -> parseMarkdownBlocks(text)
            text.contains(GENERIC_START) -> parseGenericBlocks(text)
            else -> return failed(source, ImportIssueCode.UNSUPPORTED_STRUCTURE)
        }
        if (nanos.now() - started > MAX_PARSE_NANOS) return failed(source, ImportIssueCode.TIME_LIMIT)
        if (blocks.size > MAX_ENTRIES) return failed(source, ImportIssueCode.TOO_MANY_ENTRIES)

        val candidates = mutableListOf<ImportCandidate>()
        val issues = mutableListOf<ImportIssue>()
        blocks.forEachIndexed { index, block ->
            if (block.issue != null) {
                issues += ImportIssue(block.issue, index)
            } else {
                val content = checkNotNull(block.content)
                if (content.isBlank()) issues += ImportIssue(ImportIssueCode.UNSUPPORTED_STRUCTURE, index)
                else if (content.toByteArray(StandardCharsets.UTF_8).size > MAX_ENTRY_BYTES) {
                    issues += ImportIssue(ImportIssueCode.ENTRY_TOO_LARGE, index)
                } else {
                    val instant = checkNotNull(block.instant)
                    val zone = checkNotNull(block.zone)
                    candidates += ImportCandidate(index, instant, zone, content, ImportFingerprint.compute(content, instant, zone))
                }
            }
        }
        return ParsedImport(source, candidates, issues)
    }

    private data class Block(
        val instant: Instant? = null,
        val zone: String? = null,
        val content: String? = null,
        val issue: ImportIssueCode? = null,
    )

    private fun parseTextBlocks(text: String): List<Block> = delimited(text, TEXT_START, TEXT_END).map { raw ->
        val date = lineValue(raw, "Data original:") ?: return@map Block(issue = ImportIssueCode.MISSING_DATE)
        val size = lineValue(raw, "Tamanho UTF-8:")?.substringBefore(' ')?.toIntOrNull()
            ?: return@map Block(issue = ImportIssueCode.INVALID_SIZE)
        val marker = "Conteúdo:"
        val contentStart = raw.indexOf(marker).takeIf { it >= 0 }?.let { lineEnd(raw, it + marker.length) }
            ?: return@map Block(issue = ImportIssueCode.UNSUPPORTED_STRUCTURE)
        dateBlock(date, lineValue(raw, "Fuso original:"), exactUtf8Prefix(raw.substring(contentStart), size))
    }

    private fun parseMarkdownBlocks(text: String): List<Block> {
        val starts = Regex("(?m)^## (.+)$").findAll(text).toList()
        return starts.mapIndexed { index, match ->
            val rawEnd = starts.getOrNull(index + 1)?.range?.first?.let { previousMarkdownSeparator(text, it) } ?: text.length
            val raw = text.substring(match.range.last + 1, rawEnd)
            val size = lineValue(raw, "Tamanho UTF-8:")?.substringBefore(' ')?.toIntOrNull()
                ?: return@mapIndexed Block(issue = ImportIssueCode.INVALID_SIZE)
            val sizeLine = raw.indexOf("Tamanho UTF-8:")
            val contentStart = if (sizeLine >= 0) lineEnd(raw, lineEnd(raw, sizeLine)) else -1
            if (contentStart < 0) Block(issue = ImportIssueCode.UNSUPPORTED_STRUCTURE)
            else dateBlock(
                match.groupValues[1].trim(),
                lineValue(raw, "Fuso original:"),
                exactUtf8Prefix(raw.substring(contentStart), size),
            )
        }
    }

    private fun parseGenericBlocks(text: String): List<Block> = delimited(text, GENERIC_START, GENERIC_END).map { raw ->
        val date = lineValue(raw, "Date:") ?: return@map Block(issue = ImportIssueCode.MISSING_DATE)
        val size = lineValue(raw, "Bytes:")?.toIntOrNull() ?: return@map Block(issue = ImportIssueCode.INVALID_SIZE)
        val separator = Regex("\\r?\\n\\r?\\n").find(raw)?.range?.last?.plus(1)
            ?: return@map Block(issue = ImportIssueCode.UNSUPPORTED_STRUCTURE)
        dateBlock(date, lineValue(raw, "Timezone:"), exactUtf8Prefix(raw.substring(separator), size))
    }

    private fun dateBlock(dateText: String, explicitZone: String?, content: String?): Block {
        if (content == null) return Block(issue = ImportIssueCode.INVALID_SIZE)
        val parsed = runCatching { OffsetDateTime.parse(dateText) }.getOrNull()
            ?: return Block(issue = ImportIssueCode.INVALID_DATE)
        val zone = explicitZone?.let { runCatching { ZoneId.of(it) }.getOrNull() }
            ?: if (explicitZone == null) parsed.offset else return Block(issue = ImportIssueCode.INVALID_DATE)
        if (zone.rules.getOffset(parsed.toInstant()) != parsed.offset) return Block(issue = ImportIssueCode.INVALID_DATE)
        return Block(parsed.toInstant(), zone.id, content)
    }

    private fun exactUtf8Prefix(value: String, expectedBytes: Int): String? {
        if (expectedBytes < 0 || expectedBytes > MAX_FILE_BYTES) return null
        val bytes = value.toByteArray(StandardCharsets.UTF_8)
        if (bytes.size < expectedBytes) return null
        return decode(bytes.copyOf(expectedBytes))
    }

    private fun delimited(text: String, start: String, end: String): List<String> {
        val blocks = mutableListOf<String>()
        var cursor = 0
        while (true) {
            val begin = text.indexOf(start, cursor)
            if (begin < 0) break
            val body = lineEnd(text, begin + start.length)
            val finish = text.indexOf(end, body)
            if (finish < 0) return listOf(Block(issue = ImportIssueCode.UNSUPPORTED_STRUCTURE).toString())
            blocks += text.substring(body, finish)
            cursor = finish + end.length
        }
        return blocks
    }

    private fun previousMarkdownSeparator(text: String, headingStart: Int): Int {
        val separator = text.lastIndexOf("\n---\n", headingStart)
        return if (separator >= 0) separator else headingStart
    }

    private fun lineValue(text: String, prefix: String): String? = text.lineSequence()
        .firstOrNull { it.startsWith(prefix) }
        ?.removePrefix(prefix)
        ?.trim()

    private fun occurrences(text: String, value: String): Int {
        var count = 0
        var cursor = 0
        while (true) {
            val found = text.indexOf(value, cursor)
            if (found < 0) return count
            count++
            cursor = found + value.length
        }
    }

    private fun lineEnd(text: String, from: Int): Int {
        val newline = text.indexOf('\n', from)
        return if (newline < 0) text.length else newline + 1
    }

    private fun decode(bytes: ByteArray): String? = runCatching {
        StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
            .decode(ByteBuffer.wrap(bytes)).toString()
    }.getOrNull()

    private fun looksLikeContainer(bytes: ByteArray): Boolean = CONTAINER_MAGIC.any { magic ->
        bytes.size >= magic.size && bytes.copyOfRange(0, magic.size).contentEquals(magic)
    }

    private fun unsupportedControl(char: Char): Boolean = char.code < 0x20 && char !in setOf('\n', '\r', '\t')

    private fun failed(source: ImportSource, code: ImportIssueCode) =
        ParsedImport(source, emptyList(), listOf(ImportIssue(code)))

    companion object {
        const val VERSION = "local-import-v1"
        const val MAX_FILE_BYTES = 5 * 1024 * 1024
        const val MAX_ENTRIES = 2_000
        const val MAX_ENTRY_BYTES = 256 * 1024
        const val MAX_LINES = 20_000
        private const val MAX_CONTROL_CHARS = 8
        private const val MAX_PARSE_NANOS = 5_000_000_000L
        private const val TEXT_START = "========== FIO: INÍCIO DA ENTRADA =========="
        private const val TEXT_END = "========== FIO: FIM DA ENTRADA ============="
        private const val GENERIC_START = "--- FIO ENTRY ---"
        private const val GENERIC_END = "--- FIO END ---"
        private val ACTIVE_CONTENT = Regex("(?is)<\\s*(script|iframe|object|embed|html|!doctype)|javascript\\s*:")
        private val CONTAINER_MAGIC = listOf(
            byteArrayOf(0x50, 0x4b),
            byteArrayOf(0x1f, 0x8b.toByte()),
            byteArrayOf(0x52, 0x61, 0x72, 0x21),
            byteArrayOf(0x37, 0x7a.toByte(), 0xbc.toByte(), 0xaf.toByte()),
        )
    }
}

object ImportFingerprint {
    fun compute(content: String, instant: Instant, zone: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(content.toByteArray(StandardCharsets.UTF_8))
        digest.update(0.toByte())
        digest.update(instant.toString().toByteArray(StandardCharsets.UTF_8))
        digest.update(0.toByte())
        digest.update(zone.toByteArray(StandardCharsets.UTF_8))
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
