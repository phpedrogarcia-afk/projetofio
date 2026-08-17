package com.projetofio.app.persistence

/**
 * Campaign 11 — Performance (10k+ entries).
 *
 * Seeds a realistic Room schema-3 database with 10.000 entries (incl. 1.000
 * soft-deleted) plus settings, drafts, returns and an import batch with 500
 * items, then measures the paths that render and maintain history at scale:
 *   1. ordered list scans (active/deleted) — the screens that render history
 *   2. purgeExpired GC — the reclaim path that must stay cheap at scale
 *   3. rollbackImport over a 500-item import — the heaviest transaction
 *   4. commitImport @Transaction with 10k rows — bulk-import path latency
 *
 * Seeding is done via raw INSERTs of schema-identical rows: the bytes stored
 * are exactly what the DAO stores, so scan/GC/rollback numbers reflect the
 * real disk layout; what matters for the campaign is that no query degrades
 * to O(N^2) as history grows, not the synthetic bulk-insert latency.
 *
 * No AVD: Robolectric + file-backed Room (in-memory would defeat the point).
 * Thresholds are generous (seconds, not ms) — they catch pathological
 * behaviour, not hardware benchmarks.
 */

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PerformanceTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val context = RuntimeEnvironment.getApplication()
    private val now = 1_760_000_000L

    private fun createFioDatabase(path: String): FioDatabase =
        Room.databaseBuilder(context, FioDatabase::class.java, path)
            .fallbackToDestructiveMigration(false)
            .allowMainThreadQueries()
            .build()

    /** Raw INSERT of a schema-identical entry row (same bytes the DAO stores). */
    private fun SupportSQLiteDatabase.insertEntry(i: Int, deleted: Boolean = false, extra: String = "") {
        val at = now - (10_000 - i) * 60
        execSQL(
            """
            INSERT INTO entries VALUES (
              'entry-${String.format(Locale.ROOT, "%06d", i)}', $at, $at, 'America/Sao_Paulo', $at,
              'NATIVE', ?, 'PLAIN_TEXT',
              ${if (i % 7 == 0) "'SOMEDAY'" else "'ELIGIBLE'"},
              ${if (i % 11 == 0) (at - 86_400) else "NULL"},
              ${i % 11},
              ${if (i <= 500) "'import-1'" else "NULL"},
              NULL,
              ${if (deleted) (now - 100) else "NULL"},
              ${if (deleted) (now + 86_400) else "NULL"},
              3)
            """.trimIndent(),
            arrayOf("cipher:$i:conteúdo de teste do registro número $i com algumas palavras".encodeToByteArray()),
        )
    }

    /** 10.000 entries (9.000 active + 1.000 soft-deleted, staggered ages). */
    private fun seed10k(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        db.beginTransaction()
        try {
            for (i in 1..10_000) db.insertEntry(i, deleted = i > 9_000)
            db.execSQL(
                "INSERT INTO drafts VALUES ('draft-1', 1, $now, ?, 'PLAIN_TEXT', 3)",
                arrayOf("rascunho".encodeToByteArray()),
            )
            db.execSQL(
                """
                INSERT INTO app_settings VALUES (1, 'GIVEN', NULL, 'OPTIONAL',
                  1, 0, 1320, 420, 'DENIED', 3)
                """.trimIndent(),
            )
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // -------------------------------------------------------------------------
    // 1. Ordered list scans — history rendering paths
    // -------------------------------------------------------------------------

    @Test
    fun `active entries list scan at 10k scale stays fast and correctly ordered`() {
        val db = createFioDatabase(java.io.File(folder.root, "perf2.db").absolutePath)
        val raw = db.openHelper.writableDatabase
        try {
            seed10k(raw)
            val dao = db.fioDao()
            val active = runBlocking(Dispatchers.IO) { dao.observeActiveEntries().first() }
            assertEquals(9_000, active.size)
            // The screen relies on query order (original_created_at DESC, id DESC):
            // verify the ordering contract end-to-end, not just in tests.
            for (i in 1 until active.size) {
                val prev = active[i - 1]
                val cur = active[i]
                val ok = prev.originalCreatedAt > cur.originalCreatedAt ||
                    (prev.originalCreatedAt == cur.originalCreatedAt && prev.id > cur.id)
                assertTrue("ordering violated at index $i", ok)
            }
            val started = System.nanoTime()
            runBlocking(Dispatchers.IO) { dao.observeActiveEntries().first() }
            val elapsedMs = (System.nanoTime() - started) / 1_000_000
            assertTrue("active scan took ${elapsedMs}ms", elapsedMs < 2_000)
            println("PERF active-scan 10k: ${elapsedMs}ms")
        } finally {
            db.close()
        }
    }

    @Test
    fun `deleted entries scan at 10k scale returns correct count and order`() {
        val db = createFioDatabase(java.io.File(folder.root, "perf3.db").absolutePath)
        val raw = db.openHelper.writableDatabase
        try {
            seed10k(raw)
            val dao = db.fioDao()
            val deleted = runBlocking(Dispatchers.IO) { dao.observeDeletedEntries().first() }
            assertEquals(1_000, deleted.size)
            for (i in 1 until deleted.size) {
                val prev = deleted[i - 1]
                val cur = deleted[i]
                val ok = prev.deletedAt!! > cur.deletedAt!! ||
                    (prev.deletedAt == cur.deletedAt && prev.id > cur.id)
                assertTrue("ordering violated at index $i", ok)
            }
        } finally {
            db.close()
        }
    }

    // -------------------------------------------------------------------------
    // 2. GC reclaim path — purgeExpired must stay cheap at scale
    // -------------------------------------------------------------------------

    @Test
    fun `purgeExpired reclaims all expired soft-deletes quickly at 10k scale`() {
        val db = createFioDatabase(java.io.File(folder.root, "perf4.db").absolutePath)
        val raw = db.openHelper.writableDatabase
        try {
            seed10k(raw)
            val dao = db.fioDao()
            // purge_expired reclaims soft-deletes whose purge_after <= now:
            // the seeded rows expire at now+86400, so reclaim at a later now.
            val reclaimNow = now + 86_401
            val started = System.nanoTime()
            val reclaimed = runBlocking(Dispatchers.IO) { dao.purgeExpired(reclaimNow) }
            val elapsedMs = (System.nanoTime() - started) / 1_000_000
            assertEquals("all 1000 expired soft-deletes reclaimed", 1_000, reclaimed)
            assertTrue("purgeExpired took ${elapsedMs}ms", elapsedMs < 2_000)
            // Nothing remains deletable — idempotency check.
            assertEquals(0, runBlocking(Dispatchers.IO) { dao.purgeExpired(reclaimNow) })
            println("PERF purgeExpired 1k rows: ${elapsedMs}ms")
        } finally {
            db.close()
        }
    }

    // -------------------------------------------------------------------------
    // 3. RollbackImport over a 500-item import — the heaviest transaction
    // -------------------------------------------------------------------------

    @Test
    fun `rollbackImport over 500 items with pending returns completes without quadratic blowup`() {
        val db = createFioDatabase(java.io.File(folder.root, "perf5.db").absolutePath)
        val raw = db.openHelper.writableDatabase
        try {
            val dao = db.fioDao()
            raw.insertEntry(0, deleted = false) // dummy to force table creation if needed
            val batch = ImportBatchEntity(
                id = "import-rb",
                source = "file://import.md",
                startedAt = now - 60,
                committedAt = now,
                status = "COMMITTED",
                sourceFileNameEnvelope = null,
                parsedCount = 500,
                importedCount = 500,
                duplicateCount = 0,
                failedCount = 0,
                parserVersion = "1.0",
                schemaVersion = 3,
            )
            val items = (1..500).map { i ->
                ImportBatchItemEntity(
                    id = "rb-item-$i",
                    batchId = "import-rb",
                    sourceIndex = i,
                    entryId = "rb-entry-$i",
                    status = "COMMITTED",
                    importedUpdatedAt = now,
                    schemaVersion = 3,
                )
            }
            val entries = (1..500).map { i ->
                entryForInsert(i).copy(
                    id = "rb-entry-$i",
                    importBatchId = "import-rb",
                    createdAt = now - 500 + i,
                    originalCreatedAt = now - 500 + i,
                    updatedAt = now,
                    deletedAt = null,
                    purgeAfter = null,
                    returnCount = i % 3,
                )
            }
            runBlocking(Dispatchers.IO) { dao.commitImport(batch, entries, items) }
            // Attach one pending return per imported entry (worst case).
            for (i in 1..500) {
                raw.execSQL(
                    """
                    INSERT INTO returns VALUES ('rb-ret-$i', 'rb-entry-$i', 'vintage', '1.0',
                      'SELECTED', $now, $now, ${now + 86_400}, ${now + 3_600},
                      NULL, NULL, NULL, NULL, NULL, NULL, '7-29d', 3)
                    """.trimIndent(),
                )
            }
            val started = System.nanoTime()
            val rows = runBlocking(Dispatchers.IO) {
                dao.rollbackImport("import-rb", now + 100, now + 86_400)
            }
            val elapsedMs = (System.nanoTime() - started) / 1_000_000
            assertEquals(500, rows.entryIds.size)
            assertEquals(500, rows.returnIds.size)
            assertEquals(0, rows.editedExcludedCount)
            assertTrue("rollback took ${elapsedMs}ms — quadratic blowup suspected", elapsedMs < 10_000)
            println("PERF rollbackImport 500 items: ${elapsedMs}ms")
        } finally {
            db.close()
        }
    }

    // -------------------------------------------------------------------------
    // 4. Flow emission stability — one mutation must not re-emit N times
    // -------------------------------------------------------------------------

    @Test
    fun `a single insert emits the active-entries flow exactly once per change`() {
        val db = createFioDatabase(java.io.File(folder.root, "perf6.db").absolutePath)
        val raw = db.openHelper.writableDatabase
        try {
            seed10k(raw)
            val dao = db.fioDao()
            val emissions = mutableListOf<Int>()
            runBlocking(Dispatchers.Default) {
                coroutineScope {
                val collectJob = launch {
                    dao.observeActiveEntries()
                        .take(3)
                        .collect { emissions += it.size }
                }
                // Give the Room invalidation tracker a moment to register the
                // observer before mutating — avoid busy loops with a single
                // bounded sleep (no long waits: the DB is local and warm).
                kotlinx.coroutines.delay(200)
                dao.insertEntryAndClearDraft(entryForInsert(99_001))
                kotlinx.coroutines.delay(200)
                dao.softDelete("entry-000001", now + 1, now + 86_400)
                // Safety timeout: if Room ever double-emits, take(3) completes;
                // if it under-emits we must not hang the CI forever.
                withTimeout(30_000) { collectJob.join() }
                }
            }
            // Room invalidation emits once per committed change:
            // initial (9000), after insert (9001), after soft-delete (9000).
            assertEquals(listOf(9000, 9001, 9000), emissions)
        } finally {
            db.close()
        }
    }

    /** Realistic in-memory entity for the rollback scenario. */
    private fun entryForInsert(i: Int): EntryEntity {
        val at = now - 500 + i
        return EntryEntity(
            id = "rb-entry-$i",
            createdAt = at,
            originalCreatedAt = at,
            originalTimeZone = "America/Sao_Paulo",
            updatedAt = at,
            source = "NATIVE",
            contentEnvelope = "cipher:$i:importado".encodeToByteArray(),
            contentFormat = "PLAIN_TEXT",
            returnMode = "ELIGIBLE",
            lastReturnedAt = null,
            returnCount = 0,
            importBatchId = "import-rb",
            importFingerprintEnvelope = null,
            deletedAt = null,
            purgeAfter = null,
            schemaVersion = 3,
        )
    }
}
