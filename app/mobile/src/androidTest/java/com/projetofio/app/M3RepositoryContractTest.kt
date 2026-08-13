package com.projetofio.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.ContentKeyProvider
import com.projetofio.app.domain.AgeBucket
import com.projetofio.app.domain.Entry
import com.projetofio.app.domain.EntrySource
import com.projetofio.app.domain.ImportBatch
import com.projetofio.app.domain.ImportBatchStatus
import com.projetofio.app.domain.ImportCommit
import com.projetofio.app.domain.ImportSource
import com.projetofio.app.domain.ReturnAttempt
import com.projetofio.app.domain.ReturnState
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.RoomFioRepository
import java.time.Clock
import java.time.Instant
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class M3RepositoryContractTest {
    private lateinit var database: FioDatabase
    private lateinit var repository: RoomFioRepository
    private val committedAt = Instant.parse("2026-08-13T15:00:00Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FioDatabase::class.java).allowMainThreadQueries().build()
        val key = KeyGenerator.getInstance("AES").apply { init(256) }.generateKey()
        repository = RoomFioRepository(database.fioDao(), AesGcmContentCipher(FixedProvider(key)), Clock.systemUTC())
    }

    @After
    fun close() = database.close()

    @Test
    fun atomicCommitEncryptsProvenanceAndRollbackPreservesEditedEntry() = runBlocking {
        val batch = ImportBatch(
            id = "synthetic-batch",
            source = ImportSource.TEXT,
            startedAt = committedAt.minusSeconds(1),
            committedAt = committedAt,
            status = ImportBatchStatus.COMMITTED,
            sourceFileName = "private-synthetic-name.txt",
            parsedCount = 2,
            importedCount = 2,
            duplicateCount = 0,
            failedCount = 0,
            parserVersion = "local-import-v1",
        )
        val first = imported("first", "primeiro texto", "fingerprint-one")
        val second = imported("second", "segundo texto", "fingerprint-two")
        repository.commitImport(ImportCommit(batch, listOf(first, second)))
        repository.insertReturnIfNoPending(
            ReturnAttempt(
                id = "opaque-return",
                entryId = first.id,
                state = ReturnState.SCHEDULED,
                createdAt = committedAt,
                windowStart = committedAt,
                windowEnd = committedAt.plusSeconds(3600),
                ageBucket = AgeBucket.DAYS_30_89,
            ),
        )
        repository.updateEntry(second.copy(content = "edição posterior", updatedAt = committedAt.plusSeconds(10)))

        val result = repository.rollbackImport(
            batch.id,
            committedAt.plusSeconds(20).toEpochMilli(),
            committedAt.plusSeconds(20 + 30L * 86_400).toEpochMilli(),
        )

        assertEquals(listOf(first.id), result.rolledBackEntryIds)
        assertEquals(listOf("opaque-return"), result.cancelledReturnIds)
        assertEquals(1, result.editedExcludedCount)
        assertEquals("edição posterior", repository.observeActiveEntries().first().single().content)
        assertEquals(first.id, repository.observeDeletedEntries().first().single().id)

        val sql = database.openHelper.readableDatabase
        sql.query("SELECT hex(source_file_name_envelope) FROM import_batches WHERE id = ?", arrayOf(batch.id)).use {
            it.moveToFirst()
            assertFalse(it.getString(0).contains("private-synthetic-name"))
        }
        sql.query("SELECT hex(import_fingerprint_envelope) FROM entries WHERE id = ?", arrayOf(first.id)).use {
            it.moveToFirst()
            assertFalse(it.getString(0).contains("fingerprint-one"))
        }
    }

    private fun imported(id: String, content: String, fingerprint: String) = Entry(
        id = id,
        createdAt = committedAt,
        originalCreatedAt = Instant.parse("2020-01-01T10:00:00Z"),
        originalTimeZone = "Z",
        updatedAt = committedAt,
        source = EntrySource.IMPORT_TEXT,
        content = content,
        importBatchId = "synthetic-batch",
        importFingerprint = fingerprint,
    )

    private class FixedProvider(private val key: SecretKey) : ContentKeyProvider {
        override fun keyForEncryption(): SecretKey = key
        override fun keyForDecryption(): SecretKey = key
    }
}
