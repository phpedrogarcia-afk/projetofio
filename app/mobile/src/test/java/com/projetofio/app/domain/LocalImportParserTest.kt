package com.projetofio.app.domain

import java.nio.charset.StandardCharsets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LocalImportParserTest {
    private val parser = LocalImportParser()

    @Test
    fun parsesLosslessFioTextExportWithUnicodeAndTrailingNewline() {
        val content = "palavras exatas 🌿\nsegunda linha\n"
        val document = buildString {
            appendLine("FIO — EXPORTAÇÃO LOCAL")
            appendLine("Formato: fio-export-v1")
            appendLine("Exportado em: 2026-08-13T12:00:00Z")
            appendLine()
            appendLine("========== FIO: INÍCIO DA ENTRADA ==========")
            appendLine("ID: opaque")
            appendLine("Data original: 2020-03-08T01:30:00-03:00")
            appendLine("Fuso original: America/Sao_Paulo")
            appendLine("Tamanho UTF-8: ${content.toByteArray().size} bytes")
            appendLine("Conteúdo:")
            append(content)
            appendLine("========== FIO: FIM DA ENTRADA =============")
        }

        val result = parser.parse(document.toByteArray(), ImportSource.TEXT)

        assertTrue(result.issues.isEmpty())
        assertEquals(content, result.candidates.single().content)
        assertEquals("America/Sao_Paulo", result.candidates.single().originalTimeZone)
    }

    @Test
    fun parsesLosslessMarkdownExport() {
        val content = "texto **permanece texto**"
        val document = """
            # Fio — exportação local

            - Formato: fio-export-v1
            - Exportado em: 2026-08-13T12:00:00Z

            ---

            ## 2021-11-07T01:30:00-04:00

            ID: `opaque`
            Tamanho UTF-8: ${content.toByteArray().size} bytes

            $content
        """.trimIndent()

        val result = parser.parse(document.toByteArray(), ImportSource.MARKDOWN)

        assertEquals(content, result.candidates.single().content)
        assertTrue(result.canCommit)
    }

    @Test
    fun exactFingerprintChangesForSameWordsAtDifferentTime() {
        fun doc(date: String) = """
            --- FIO ENTRY ---
            Date: $date
            Bytes: 5

            igual
            --- FIO END ---
        """.trimIndent().toByteArray()
        val first = parser.parse(doc("2020-01-01T10:00:00Z"), ImportSource.TEXT).candidates.single()
        val second = parser.parse(doc("2020-01-02T10:00:00Z"), ImportSource.TEXT).candidates.single()
        assertFalse(first.fingerprint == second.fingerprint)
    }

    @Test
    fun malformedUtf8AndContainersFailBeforeCandidates() {
        val malformed = parser.parse(byteArrayOf(0xC3.toByte(), 0x28), ImportSource.TEXT)
        val zip = parser.parse(byteArrayOf(0x50, 0x4b, 0x03, 0x04), ImportSource.TEXT)
        assertEquals(ImportIssueCode.MALFORMED_UTF8, malformed.issues.single().code)
        assertEquals(ImportIssueCode.UNSUPPORTED_CONTAINER, zip.issues.single().code)
    }

    @Test
    fun activeContentMissingDateAndOversizedInputFailClosed() {
        val active = parser.parse("<script>alert(1)</script>".toByteArray(), ImportSource.MARKDOWN)
        val noDate = parser.parse("""
            --- FIO ENTRY ---
            Bytes: 5

            texto
            --- FIO END ---
        """.trimIndent().toByteArray(), ImportSource.TEXT)
        val oversized = parser.parse(ByteArray(LocalImportParser.MAX_FILE_BYTES + 1), ImportSource.TEXT)
        assertEquals(ImportIssueCode.ACTIVE_CONTENT, active.issues.single().code)
        assertEquals(ImportIssueCode.MISSING_DATE, noDate.issues.single().code)
        assertEquals(ImportIssueCode.FILE_TOO_LARGE, oversized.issues.single().code)
    }

    @Test
    fun parserTimeLimitFailsClosed() {
        var tick = 0L
        val timed = LocalImportParser(MonotonicNanos { tick.also { tick += 6_000_000_000L } })
        val bytes = """
            --- FIO ENTRY ---
            Date: 2020-01-01T10:00:00Z
            Bytes: 5

            texto
            --- FIO END ---
        """.trimIndent().toByteArray(StandardCharsets.UTF_8)
        assertEquals(ImportIssueCode.TIME_LIMIT, timed.parse(bytes, ImportSource.TEXT).issues.single().code)
    }

    @Test
    fun entryCountLineAndEntryByteLimitsFailClosed() {
        val tooManyEntries = buildString {
            repeat(LocalImportParser.MAX_ENTRIES + 1) {
                appendLine("--- FIO ENTRY ---")
                appendLine("Date: 2020-01-01T10:00:00Z")
                appendLine("Bytes: 1")
                appendLine()
                appendLine("x")
                appendLine("--- FIO END ---")
            }
        }.toByteArray()
        val tooManyLines = ("line\n".repeat(LocalImportParser.MAX_LINES + 1)).toByteArray()
        val largeContent = "x".repeat(LocalImportParser.MAX_ENTRY_BYTES + 1)
        val tooLargeEntry = """
            --- FIO ENTRY ---
            Date: 2020-01-01T10:00:00Z
            Bytes: ${largeContent.length}

            $largeContent
            --- FIO END ---
        """.trimIndent().toByteArray()

        assertEquals(ImportIssueCode.TOO_MANY_ENTRIES, parser.parse(tooManyEntries, ImportSource.TEXT).issues.single().code)
        assertEquals(ImportIssueCode.TOO_MANY_LINES, parser.parse(tooManyLines, ImportSource.TEXT).issues.single().code)
        assertEquals(ImportIssueCode.ENTRY_TOO_LARGE, parser.parse(tooLargeEntry, ImportSource.TEXT).issues.single().code)
    }
}
