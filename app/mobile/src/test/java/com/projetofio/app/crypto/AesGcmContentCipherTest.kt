package com.projetofio.app.crypto

import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

class AesGcmContentCipherTest {
    private val key = SecretKeySpec(ByteArray(32) { (it * 7 + 3).toByte() }, "AES")
    private val cipher = AesGcmContentCipher(FixedProvider(key))

    @Test
    fun exactUnicodeRoundTrip() {
        val source = "Chuva — café ☕\n  espaço preservado  "
        val envelope = cipher.seal(source, RecordKind.ENTRY, "entry-a", 1)
        assertEquals(source, cipher.open(envelope, RecordKind.ENTRY, "entry-a", 1))
    }

    @Test
    fun everySealUsesDifferentIv() {
        val first = cipher.seal("texto sintético", RecordKind.ENTRY, "entry-a", 1)
        val second = cipher.seal("texto sintético", RecordKind.ENTRY, "entry-a", 1)
        assertFalse(first.contentEquals(second))
        assertFalse(EnvelopeCodec.decode(first).iv.contentEquals(EnvelopeCodec.decode(second).iv))
    }

    @Test
    fun associatedDataIsStableAndLengthDelimited() {
        val first = cipher.associatedData(RecordKind.ENTRY, "ab", 1)
        val second = cipher.associatedData(RecordKind.ENTRY, "a", 1)
        assertFalse(first.contentEquals(second))
        assertArrayEquals(first, cipher.associatedData(RecordKind.ENTRY, "ab", 1))
    }

    @Test
    fun wrongAssociatedDataStopsSafely() {
        val envelope = cipher.seal("texto sintético", RecordKind.ENTRY, "entry-a", 1)
        assertThrows(CryptoFailure.AuthenticationFailed::class.java) {
            cipher.open(envelope, RecordKind.ENTRY, "entry-b", 1)
        }
    }

    @Test
    fun tamperStopsSafely() {
        val envelope = cipher.seal("texto sintético", RecordKind.DRAFT, "draft-a", 1)
        envelope[envelope.lastIndex] = (envelope.last().toInt() xor 1).toByte()
        assertThrows(CryptoFailure.AuthenticationFailed::class.java) {
            cipher.open(envelope, RecordKind.DRAFT, "draft-a", 1)
        }
    }

    @Test
    fun truncationIsRejected() {
        val envelope = cipher.seal("texto sintético", RecordKind.ENTRY, "entry-a", 1)
        assertThrows(CryptoFailure.InvalidEnvelope::class.java) {
            cipher.open(envelope.copyOf(envelope.size - 3), RecordKind.ENTRY, "entry-a", 1)
        }
    }

    @Test
    fun unsupportedEnvelopeVersionIsRejected() {
        val envelope = cipher.seal("texto sintético", RecordKind.ENTRY, "entry-a", 1)
        envelope[4] = 99
        assertThrows(CryptoFailure.UnsupportedVersion::class.java) {
            cipher.open(envelope, RecordKind.ENTRY, "entry-a", 1)
        }
    }

    @Test
    fun missingKeyDoesNotBecomeEmptyContent() {
        val missing = AesGcmContentCipher(object : ContentKeyProvider {
            override fun keyForEncryption(): SecretKey = throw CryptoFailure.MissingKey()
            override fun keyForDecryption(): SecretKey = throw CryptoFailure.MissingKey()
        })
        val envelope = cipher.seal("texto sintético", RecordKind.ENTRY, "entry-a", 1)
        assertThrows(CryptoFailure.MissingKey::class.java) {
            missing.open(envelope, RecordKind.ENTRY, "entry-a", 1)
        }
    }

    private class FixedProvider(private val key: SecretKey) : ContentKeyProvider {
        override fun keyForEncryption(): SecretKey = key
        override fun keyForDecryption(): SecretKey = key
    }
}
