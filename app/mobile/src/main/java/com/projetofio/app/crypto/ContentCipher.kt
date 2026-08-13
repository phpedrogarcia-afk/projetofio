package com.projetofio.app.crypto

enum class RecordKind(val wireName: String) {
    ENTRY("entry"),
    DRAFT("draft"),
    IMPORT_FINGERPRINT("import-fingerprint"),
    IMPORT_SOURCE_FILE("import-source-file"),
}

interface ContentCipher {
    fun seal(plaintext: String, kind: RecordKind, recordId: String, schemaVersion: Int): ByteArray
    fun open(envelope: ByteArray, kind: RecordKind, recordId: String, schemaVersion: Int): String
}

sealed class CryptoFailure(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class MissingKey : CryptoFailure("The content key is unavailable")
    class InvalidEnvelope : CryptoFailure("The encrypted record envelope is invalid")
    class UnsupportedVersion : CryptoFailure("The encrypted record version is unsupported")
    class AuthenticationFailed(cause: Throwable) : CryptoFailure("Encrypted record authentication failed", cause)
    class KeyUnavailable(cause: Throwable) : CryptoFailure("The content key cannot be used", cause)
}
