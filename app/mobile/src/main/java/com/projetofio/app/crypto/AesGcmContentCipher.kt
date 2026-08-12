package com.projetofio.app.crypto

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets
import javax.crypto.AEADBadTagException
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

interface ContentKeyProvider {
    fun keyForEncryption(): SecretKey
    fun keyForDecryption(): SecretKey
}

class AesGcmContentCipher(
    private val keys: ContentKeyProvider,
) : ContentCipher {
    override fun seal(
        plaintext: String,
        kind: RecordKind,
        recordId: String,
        schemaVersion: Int,
    ): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, keys.keyForEncryption())
        cipher.updateAAD(associatedData(kind, recordId, schemaVersion))
        val ciphertext = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        return EnvelopeCodec.encode(cipher.iv, ciphertext)
    }

    override fun open(
        envelope: ByteArray,
        kind: RecordKind,
        recordId: String,
        schemaVersion: Int,
    ): String {
        val decoded = EnvelopeCodec.decode(envelope)
        return try {
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, keys.keyForDecryption(), GCMParameterSpec(TAG_BITS, decoded.iv))
            cipher.updateAAD(associatedData(kind, recordId, schemaVersion))
            val plaintext = cipher.doFinal(decoded.ciphertext)
            StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(java.nio.ByteBuffer.wrap(plaintext))
                .toString()
        } catch (error: AEADBadTagException) {
            throw CryptoFailure.AuthenticationFailed(error)
        } catch (error: CryptoFailure) {
            throw error
        } catch (error: Exception) {
            throw CryptoFailure.InvalidEnvelope()
        }
    }

    internal fun associatedData(kind: RecordKind, recordId: String, schemaVersion: Int): ByteArray =
        ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.writeInt(ENVELOPE_VERSION)
                output.writeLengthPrefixed(kind.wireName)
                output.writeLengthPrefixed(recordId)
                output.writeInt(schemaVersion)
            }
            bytes.toByteArray()
        }

    private fun DataOutputStream.writeLengthPrefixed(value: String) {
        val encoded = value.toByteArray(StandardCharsets.UTF_8)
        writeInt(encoded.size)
        write(encoded)
    }

    private companion object {
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val TAG_BITS = 128
        const val ENVELOPE_VERSION = 1
    }
}

internal object EnvelopeCodec {
    private val magic = byteArrayOf(0x46, 0x49, 0x4F, 0x31)

    data class Decoded(val iv: ByteArray, val ciphertext: ByteArray)

    fun encode(iv: ByteArray, ciphertext: ByteArray): ByteArray {
        require(iv.size in 12..16)
        require(ciphertext.isNotEmpty())
        return ByteArrayOutputStream().use { bytes ->
            DataOutputStream(bytes).use { output ->
                output.write(magic)
                output.writeByte(1)
                output.writeByte(iv.size)
                output.writeInt(ciphertext.size)
                output.write(iv)
                output.write(ciphertext)
            }
            bytes.toByteArray()
        }
    }

    fun decode(envelope: ByteArray): Decoded {
        try {
            DataInputStream(ByteArrayInputStream(envelope)).use { input ->
                val actualMagic = ByteArray(magic.size).also(input::readFully)
                if (!actualMagic.contentEquals(magic)) throw CryptoFailure.InvalidEnvelope()
                if (input.readUnsignedByte() != 1) throw CryptoFailure.UnsupportedVersion()
                val ivSize = input.readUnsignedByte()
                val ciphertextSize = input.readInt()
                if (ivSize !in 12..16 || ciphertextSize < 16) throw CryptoFailure.InvalidEnvelope()
                if (input.available() != ivSize + ciphertextSize) throw CryptoFailure.InvalidEnvelope()
                val iv = ByteArray(ivSize).also(input::readFully)
                val ciphertext = ByteArray(ciphertextSize).also(input::readFully)
                if (input.available() != 0) throw CryptoFailure.InvalidEnvelope()
                return Decoded(iv, ciphertext)
            }
        } catch (error: CryptoFailure) {
            throw error
        } catch (_: Exception) {
            throw CryptoFailure.InvalidEnvelope()
        }
    }
}
