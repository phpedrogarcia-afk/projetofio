package com.projetofio.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projetofio.app.application.FioService
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.AndroidKeystoreKeyProvider
import com.projetofio.app.crypto.ContentKeyProvider
import com.projetofio.app.crypto.CryptoFailure
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.DatabasePreflight
import com.projetofio.app.persistence.RoomFioRepository
import java.security.KeyStore
import java.security.MessageDigest
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdverseConditionTest {
    @Test
    fun sqliteFullDuringSaveKeepsLastRecoverableDraftAndNoEntry() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "fio-synthetic-storage-full.db"
        context.deleteDatabase(databaseName)
        val database = Room.databaseBuilder(context, FioDatabase::class.java, databaseName)
            .setJournalMode(androidx.room.RoomDatabase.JournalMode.TRUNCATE)
            .build()
        try {
            val service = service(database)
            val lastRecoverable = "rascunho sintético anterior ao armazenamento cheio"
            service.autosaveDraft(lastRecoverable)
            val sqlite = database.openHelper.writableDatabase
            val pageCount = sqlite.query("PRAGMA page_count").use { cursor ->
                check(cursor.moveToFirst())
                cursor.getLong(0)
            }
            sqlite.query("PRAGMA max_page_count = $pageCount").use { cursor ->
                check(cursor.moveToFirst())
                assertEquals(pageCount, cursor.getLong(0))
            }

            val failure = runCatching {
                service.saveEntry("x".repeat(2_000_000))
            }.exceptionOrNull()

            assertNotNull(failure)
            assertEquals(lastRecoverable, service.loadDraft()?.content)
            assertTrue(service.observeActiveEntries().first().isEmpty())
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun corruptDatabaseOpenFailsWithoutReplacingTheFile() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "fio-synthetic-corrupt-open.db"
        context.deleteDatabase(databaseName)
        val databaseFile = context.getDatabasePath(databaseName)
        databaseFile.parentFile?.mkdirs()
        val corruptBytes = ByteArray(8_192) { index -> (index * 31 + 7).toByte() }
        databaseFile.writeBytes(corruptBytes)
        val before = sha256(databaseFile.readBytes())
        try {
            val failure = runCatching {
                DatabasePreflight.verifyExistingDatabase(context, databaseName)
            }.exceptionOrNull()

            assertNotNull(failure)
            assertTrue(databaseFile.exists())
            assertEquals(before, sha256(databaseFile.readBytes()))
        } finally {
            context.deleteDatabase(databaseName)
        }
    }

    @Test
    fun missingKeyWithPersistentCiphertextFailsWithoutReplacement() {
        val alias = "fio_synthetic_missing_key_test"
        val store = androidKeyStore()
        store.deleteEntry(alias)
        try {
            val provider = AndroidKeystoreKeyProvider(
                hasPersistentCiphertext = { true },
                keyAlias = alias,
            )

            val failure = runCatching { provider.keyForEncryption() }.exceptionOrNull()

            assertTrue(failure is CryptoFailure.MissingKey)
            assertFalse(androidKeyStore().containsAlias(alias))
        } finally {
            androidKeyStore().deleteEntry(alias)
        }
    }

    @Test
    fun firstUseCreatesNonExportableKeystoreKey() {
        val alias = "fio_synthetic_first_use_key_test"
        androidKeyStore().deleteEntry(alias)
        try {
            val provider = AndroidKeystoreKeyProvider(
                hasPersistentCiphertext = { false },
                keyAlias = alias,
            )

            val key = provider.keyForEncryption()

            assertEquals("AES", key.algorithm)
            assertTrue(androidKeyStore().containsAlias(alias))
            assertTrue(key.encoded == null)
        } finally {
            androidKeyStore().deleteEntry(alias)
        }
    }

    @Test
    fun entryInsertRollsBackWhenDraftDeletionFails() = runBlocking {
        val database = inMemoryDatabase()
        try {
            val service = service(database)
            service.autosaveDraft("rascunho sintético mais recente")
            database.openHelper.writableDatabase.execSQL(
                "CREATE TRIGGER synthetic_fail_draft_delete " +
                    "BEFORE DELETE ON drafts BEGIN SELECT RAISE(ABORT, 'synthetic failure'); END",
            )

            val failure = runCatching { service.saveEntry("rascunho sintético mais recente") }.exceptionOrNull()

            assertNotNull(failure)
            assertEquals("rascunho sintético mais recente", service.loadDraft()?.content)
            assertTrue(service.observeActiveEntries().first().isEmpty())
            assertEquals(1, database.fioDao().encryptedRecordCount())
        } finally {
            database.close()
        }
    }

    @Test
    fun corruptedEnvelopeFailsClosedAndKeepsStoredRow() = runBlocking {
        val database = inMemoryDatabase()
        try {
            val service = service(database)
            val saved = service.saveEntry("conteúdo sintético preservado")
            database.openHelper.writableDatabase.execSQL(
                "UPDATE entries SET content_envelope = X'0001' WHERE id = ?",
                arrayOf(saved.id),
            )

            val failure = runCatching { service.observeActiveEntries().first() }.exceptionOrNull()

            assertTrue(failure is CryptoFailure.InvalidEnvelope)
            assertNotNull(database.fioDao().findEntry(saved.id))
            assertEquals(1, database.fioDao().encryptedRecordCount())
        } finally {
            database.close()
        }
    }

    private fun inMemoryDatabase(): FioDatabase {
        val context = ApplicationProvider.getApplicationContext<Context>()
        return Room.inMemoryDatabaseBuilder(context, FioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    private fun service(database: FioDatabase): FioService {
        val generator = KeyGenerator.getInstance("AES").apply { init(256) }
        val cipher = AesGcmContentCipher(FixedProvider(generator.generateKey()))
        val clock = Clock.fixed(Instant.parse("2026-08-10T12:30:00Z"), ZoneOffset.UTC)
        return FioService(
            RoomFioRepository(database.fioDao(), cipher, clock),
            clock,
            IdGenerator { "synthetic-${System.nanoTime()}" },
        )
    }

    private fun androidKeyStore(): KeyStore =
        KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun sha256(bytes: ByteArray): String =
        MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }

    private class FixedProvider(private val key: SecretKey) : ContentKeyProvider {
        override fun keyForEncryption(): SecretKey = key
        override fun keyForDecryption(): SecretKey = key
    }
}
