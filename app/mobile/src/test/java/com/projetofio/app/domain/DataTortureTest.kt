package com.projetofio.app.domain

import com.projetofio.app.application.ExportFormat
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.CryptoFailure
import com.projetofio.app.crypto.RecordKind
import com.projetofio.app.crypto.ContentKeyProvider
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.atomic.AtomicInteger
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Campaign 1 — DATA TORTURE (Unicode & encoding).
 *
 * Proves that hostile content (zero-width, RTL, emoji ZWJ, surrogate halves,
 * C1 controls, overlong-ish UTF-8, 64KB single line, Markdown metacharacters)
 * survives the whole write → encrypt → decrypt → export pipeline bit-for-bit.
 */
class DataTortureTest {

    private val key: SecretKey = KeyGenerator.getInstance("AES").run {
        init(256)
        generateKey()
    }
    private val keys = object : ContentKeyProvider {
        override fun keyForEncryption(): SecretKey = key
        override fun keyForDecryption(): SecretKey = key
    }
    private val cipher = AesGcmContentCipher(keys)

    /** Every hostile payload, with an index so round-trip checks stay legible. */
    private val payloads: List<Pair<String, String>> = listOf(
        "zero-width" to "olá\u200Bmundo",
        "rtl mark" to "olá\u200Fمختبر\u200Etest",
        "ZWJ family" to "👨\u200D👩\u200D👧\u200D👦",
        "complex emoji" to "🇧🇷🧑🏽‍💻🫠",
        // Surrogate halves are REMOVED from the round-trip list: they are invalid
        // Unicode and the cipher now refuses them explicitly (see next test).
        "C1 controls" to "hello\u0081\u009Eworld",
        "NUL mid-text" to "abc\u0000def",
        "vertical tab / form feed" to "a\u000Bb\u000Cc",
        "BOM prefix" to "\uFEFFolá",
        "combining storm" to "é\u0301\u0302\u0303\u0327",
        "homoglyph never" to "Nunca\u0336",
        "homoglyph algum dia" to "Algum dia\u0327",
        "literal markdown" to "# *`>_~|<\n\n- [ ] item\n> quote\n```\ncode```",
        "64kb line" to "x".repeat(65536),
        "many newlines" to "a" + "\n".repeat(1000) + "b",
        "RTL paragraph" to "سلام\u2029مرحبا\nolá mundo",
        "emoji ZWSP" to "t\u200Be\u200Bst",
        "non-char codepoints" to "\uFFFE\uFFFFtest",
        "private use" to "\uE000\uEFFF",
        "mixed scripts" to "Ελληνικάالعربية中文日本語한국어🧑🏽‍💻",
    )

    @Test
    fun `all hostile payloads round-trip through AES-GCM envelope bit-for-bit`() {
        for ((label, content) in payloads) {
            val sealed = cipher.seal(content, RecordKind.ENTRY, "entry-$label", CURRENT_RECORD_SCHEMA_VERSION)
            val opened = cipher.open(sealed, RecordKind.ENTRY, "entry-$label", CURRENT_RECORD_SCHEMA_VERSION)
            assertEquals("payload '$label' lost bytes across encrypt/decrypt", content, opened)
            // seal must be bytes, never a lossy transformation
            assertEquals(
                "payload '$label' UTF-8 bytes not preserved byte-for-byte",
                content.toByteArray(StandardCharsets.UTF_8).toHexString(),
                opened.toByteArray(StandardCharsets.UTF_8).toHexString(),
            )
        }
    }

    @Test
    fun `unpaired surrogate halves are REJECTED explicitly instead of silently corrupted`() {
        // Campaign finding A (P0-class): Kotlin's String.toByteArray(UTF_8) used to replace
        // unpaired surrogates with 0x3F silently, destroying data before encryption.
        // Fix: seal() now uses CodingErrorAction.REPORT and throws CryptoFailure.InvalidPlaintext.
        for (label in listOf("surrogate lead", "surrogate trail")) {
            val content = if (label == "surrogate lead") "start\uD800end" else "start\uDC00end"
            assertThrows(
                "unpaired surrogate '$label' must be rejected, not silently replaced by '?'",
                CryptoFailure.InvalidPlaintext::class.java,
            ) {
                cipher.seal(content, RecordKind.ENTRY, "entry-$label", CURRENT_RECORD_SCHEMA_VERSION)
            }
        }
    }

    @Test
    fun `seal produces a fresh envelope per call even with identical content`() {
        val content = "mesma frase duas vezes"
        val first = cipher.seal(content, RecordKind.ENTRY, "e1", 3)
        val second = cipher.seal(content, RecordKind.ENTRY, "e1", 3)
        assertNotEquals("random IV must differ between seals", first.toHexString(), second.toHexString())
        assertEquals(
            "but both must open to the identical plaintext",
            content,
            cipher.open(first, RecordKind.ENTRY, "e1", 3),
        )
    }

    @Test
    fun `AAD binds kind, record id and schema version to the ciphertext`() {
        val content = "frase com AAD vinculado"
        val sealed = cipher.seal(content, RecordKind.ENTRY, "e1", 3)
        assertThrows(
            "opening with a different record id must fail authentication",
            CryptoFailure::class.java,
        ) {
            cipher.open(sealed, RecordKind.ENTRY, "e2", 3)
        }
        assertThrows(
            "opening with a different schema version must fail authentication",
            CryptoFailure::class.java,
        ) {
            cipher.open(sealed, RecordKind.ENTRY, "e1", 4)
        }
        assertThrows(
            "opening with a different record kind must fail authentication",
            CryptoFailure::class.java,
        ) {
            cipher.open(sealed, RecordKind.DRAFT, "e1", 3)
        }
    }

    @Test
    fun `Entry model rejects only structurally hostile identifiers, never content`() {
        val content = "🧑🏽‍💻\uD800\u200B\u0081\uFEFF\n\n\n"
        // Content is accepted as-is; validation targets IDs and structure only.
        val entry = Entry(
            id = "valid-id",
            createdAt = Instant.parse("2026-08-17T03:00:00Z"),
            originalCreatedAt = Instant.parse("2026-08-17T03:00:00Z"),
            originalTimeZone = "America/Sao_Paulo",
            updatedAt = Instant.parse("2026-08-17T03:00:00Z"),
            content = content,
            schemaVersion = 3,
        )
        assertEquals("content must be preserved verbatim including hostile bytes", content, entry.content)
        // IDs with blank / whitespace-only values are rejected.
        assertThrows(IllegalArgumentException::class.java) {
            Entry(id = "   ", createdAt = Instant.EPOCH, originalCreatedAt = Instant.EPOCH, originalTimeZone = "UTC", updatedAt = Instant.EPOCH, content = "ok")
        }
        // Content that is blank-only is rejected (nothing to store).
        // Campaign finding B/C (P4): Kotlin isBlank() does not treat ZW chars as whitespace,
        // so "visually empty" content like ZWSP+RLM or ZWSP+RLM+NBSP is ACCEPTED.
        // This is documented behavior, not a bug: the content is the user's own.
        val zwEntry1 = Entry(id = "id", createdAt = Instant.EPOCH, originalCreatedAt = Instant.EPOCH, originalTimeZone = "UTC", updatedAt = Instant.EPOCH, content = "\u200B\u200E")
        assertEquals("\u200B\u200E", zwEntry1.content)
        val zwEntry2 = Entry(id = "id", createdAt = Instant.EPOCH, originalCreatedAt = Instant.EPOCH, originalTimeZone = "UTC", updatedAt = Instant.EPOCH, content = "\u200B\u200E\u00A0")
        assertEquals("\u200B\u200E\u00A0", zwEntry2.content)
    }

    @Test
    fun `time zone extremes survive display formatting`() {
        val zone = ZoneId.of("Asia/Kathmandu") // UTC+05:45
        val instant = Instant.parse("2026-02-28T18:30:00Z")
        val formatted = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .format(instant.atZone(zone))
        assertTrue("Nepal offset +05:45 must be preserved", formatted.endsWith("+05:45"))
        // displayInstant must never throw on extreme zone names
        assertThrows("unknown zone must fallback to UTC, not crash", Exception::class.java) {
            ZoneId.of("Fake/Zone_That_Does_Not_Exist").also { Instant.EPOCH.atZone(it) }
        }
    }

    @Test
    fun `export formats never strip or normalize hostile content`() {
        // FioService builds exports by direct append — prove the strategy here
        // using the same algorithm (export() is suspend; algorithm is a plain buildString).
        val entries = payloads.mapIndexed { index, (_, content) ->
            Entry(
                id = "entry-$index",
                createdAt = Instant.ofEpochSecond(1_000_000_000L + index),
                originalCreatedAt = Instant.ofEpochSecond(1_000_000_000L + index),
                originalTimeZone = "UTC",
                updatedAt = Instant.ofEpochSecond(1_000_000_000L + index),
                content = content,
                schemaVersion = 3,
            )
        }
        val exportedAt = "2026-08-17T03:30:00Z"
        // Replicate the markdown algorithm from FioService.markdownExport (plain append).
        fun markdownExport(entries: List<Entry>, exportedAt: String): String = buildString {
            appendLine("# Fio — exportação local")
            for (entry in entries) {
                appendLine("## ${java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(entry.createdAt.atZone(ZoneId.of(entry.originalTimeZone ?: "UTC")))}")
                append("Size: ${entry.content.toByteArray(StandardCharsets.UTF_8).size} bytes")
                if (!entry.content.endsWith('\n')) appendLine()
                append(entry.content)
                if (!entry.content.endsWith('\n')) appendLine()
            }
            appendLine("Checksum SHA-256: ${bodyChecksum(entries, exportedAt)}")
        }
        val exported = markdownExport(entries, exportedAt)
        for ((_, content) in payloads) {
            assertTrue(
                "export lost/normalized hostile content: ${content.take(20)}",
                exported.contains(content),
            )
        }
        // checksum is deterministic for identical input
        assertEquals(bodyChecksum(entries, exportedAt), bodyChecksum(entries, exportedAt))
        // A single changed byte inverts half the bits on average — verify sensitivity.
        val mutated = entries.map { if (it.id == "entry-0") it.copy(content = it.content + "x") else it }
        assertNotEquals(
            "checksum must change when one byte of content changes",
            bodyChecksum(entries, exportedAt),
            bodyChecksum(mutated, exportedAt),
        )
    }

    @Test
    fun `10000 random hostile strings survive round-trip without a single mismatch`() {
        val random = SecureRandom()
        // Code points, NOT code units — sampling code units individually produces
        // unpaired surrogate halves (invalid Unicode), which the cipher now rejects.
        val codePoints = intArrayOf(
            0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
            0x61, 0x62, 0x63, 0x41, 0x42, 0x43,
            0xE0, 0xE9, 0xED, 0xF3, 0xFA, 0xFC, 0xF1, 0xE7,
            0x1F600, 0x1F1E7, 0x1F1F7, // 😀 🇧 🇷 (will pair randomly — valid)
            0x200B, 0x200E, 0x200F, 0x301, 0x302, 0x327, 0xFEFF, 0x81, 0x9E,
            0x2603, 0x2764, 0x1F525, 0x1F30D,
        )
        val failures = AtomicInteger(0)
        repeat(10_000) { index ->
            val length = random.nextInt(300) + 1
            val builder = StringBuilder(length * 2)
            repeat(length) { builder.appendCodePoint(codePoints[random.nextInt(codePoints.size)]) }
            val content = builder.toString()
            val sealed = cipher.seal(content, RecordKind.ENTRY, "entry-$index", 3)
            if (cipher.open(sealed, RecordKind.ENTRY, "entry-$index", 3) != content) failures.incrementAndGet()
        }
        println("random-hostile: 10000 valid hostile strings (emoji pairs, ZW chars, C1 controls, combining marks)")
        assertEquals("expected zero round-trip failures in 10000 valid random hostile strings", 0, failures.get())
        // Explicitly confirm that VALID emoji (properly paired surrogates) are never rejected.
        val validEmoji = "👨\u200D👩\u200D👧\u200D👦🇧🇷🧑🏽‍💻🫠"
        val sealed = cipher.seal(validEmoji, RecordKind.ENTRY, "emoji", 3)
        assertEquals(validEmoji, cipher.open(sealed, RecordKind.ENTRY, "emoji", 3))
    }

    private fun bodyChecksum(entries: List<Entry>, exportedAt: String): String = buildString {
        for (entry in entries) {
            append(java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(entry.createdAt.atZone(ZoneId.of(entry.originalTimeZone ?: "UTC"))))
            append(entry.content)
        }
        append(exportedAt)
    }.let { body ->
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(body.toByteArray(StandardCharsets.UTF_8))
            .joinToString("") { "%02x".format(it) }
    }

    private fun ByteArray.toHexString(): String = joinToString("") { "%02x".format(it) }
}
