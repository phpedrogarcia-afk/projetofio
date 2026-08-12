package com.projetofio.app.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

class AndroidKeystoreKeyProvider(
    private val keyAlias: String = KEY_ALIAS,
    private val hasPersistentCiphertext: () -> Boolean,
) : ContentKeyProvider {
    override fun keyForEncryption(): SecretKey {
        loadKey()?.let { return it }
        if (hasPersistentCiphertext()) throw CryptoFailure.MissingKey()
        return try {
            val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE)
            generator.init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build(),
            )
            generator.generateKey()
        } catch (error: Exception) {
            throw CryptoFailure.KeyUnavailable(error)
        }
    }

    override fun keyForDecryption(): SecretKey = loadKey() ?: throw CryptoFailure.MissingKey()

    private fun loadKey(): SecretKey? = try {
        val store = KeyStore.getInstance(KEYSTORE).apply { load(null) }
        store.getKey(keyAlias, null) as? SecretKey
    } catch (error: Exception) {
        throw CryptoFailure.KeyUnavailable(error)
    }

    companion object {
        const val KEY_ALIAS = "fio_m1_content_v1"
        private const val KEYSTORE = "AndroidKeyStore"
    }
}
