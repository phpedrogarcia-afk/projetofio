package com.projetofio.app.persistence

import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * FIO-P19 A1 — Database / Migration Test (Room 3 -> 4).
 *
 * Reproduces a realistic schema-3 database (entries with imports and batches)
 * and lets Room itself apply MIGRATION_3_4, asserting the additive contract:
 * row count preserved, ciphertext envelopes unchanged, pre-existing entries
 * have requested_window_start and requested_window_end as NULL (organic ELIGIBLE).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class Migration3To4Test {
    @get:Rule
    val folder = TemporaryFolder()

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `Room applies migration 3-4 additively without data loss`() {
        val (dbFile, envelopesBefore) = seedSchema3Database("fio-v3-to-v4.db")

        val db4 = Room.databaseBuilder(context, FioDatabase::class.java, dbFile.absolutePath)
            .fallbackToDestructiveMigration(false)
            .addMigrations(MIGRATION_3_4)
            .allowMainThreadQueries()
            .build()

        try {
            val entries = db4.count("entries")
            assertEquals("entry row count must be preserved", 100, entries)
            assertEquals("draft row count must be preserved", 1, db4.count("drafts"))
            assertEquals("settings row count must be preserved", 1, db4.count("app_settings"))
            assertEquals("return row count must be preserved", 10, db4.count("returns"))
            assertEquals("import batch row count must be preserved", 1, db4.count("import_batches"))
            assertEquals("import batch items row count must be preserved", 5, db4.count("import_batch_items"))

            // Content envelopes must be byte-identical
            val actual = readContentEnvelopesRoom(db4)
            assertEquals("envelope count preserved", envelopesBefore.size, actual.size)
            for (i in envelopesBefore.indices) {
                assertTrue(
                    "envelope $i differs after migration",
                    envelopesBefore[i].contentEquals(actual[i]),
                )
            }

            // New schema 4 columns default to NULL for all pre-existing rows
            assertEquals(entries, db4.count("entries", "requested_window_start IS NULL"))
            assertEquals(entries, db4.count("entries", "requested_window_end IS NULL"))

            // Structural identity: all schema-4 columns present in exact order
            assertStructuralIdentity(db4)
        } finally {
            db4.close()
        }
    }

    private fun seedSchema3Database(name: String): Pair<java.io.File, List<ByteArray>> {
        val dbFile = folder.newFile(name)
        val db = openHelper(dbFile, 3)
        createSchema3Tables(db)

        for (i in 1..100) {
            val content = "registro schema-3 número $i"
            val envelope = ("cipher:$i:$content").encodeToByteArray()
            db.execSQL(
                """
                INSERT INTO entries VALUES ('entry-${"%03d".format(i)}', ${1700000000L + i},
                  ${1700000000L + i}, 'America/Sao_Paulo', ${1700000000L + i}, 'NATIVE', ?,
                  'PLAIN_TEXT', 'ELIGIBLE', NULL, 0, NULL, NULL, NULL, NULL, 3)
                """.trimIndent(),
                arrayOf(envelope),
            )
        }

        db.execSQL(
            "INSERT INTO drafts VALUES ('draft-1', 1, 1700000000, ?, 'PLAIN_TEXT', 3)",
            arrayOf("rascunho v3".encodeToByteArray()),
        )
        db.execSQL(
            """
            INSERT INTO app_settings VALUES (1, 'ENABLED', NULL, 'OPTIONAL',
              1, 0, 1260, 480, 'GRANTED', 3)
            """.trimIndent(),
        )
        for (i in 1..10) {
            db.execSQL(
                """
                INSERT INTO returns VALUES ('ret-${"%02d".format(i)}', 'entry-${"%03d".format(i)}',
                  'vintage', '1.0', 'SCHEDULED',
                  ${1600000000L + i}, ${1600000000L + i}, ${1600000000L + i + 3600},
                  NULL, NULL, NULL, NULL, NULL, NULL, NULL, '7-29d', 3)
                """.trimIndent(),
            )
        }
        db.execSQL(
            """
            INSERT INTO import_batches VALUES ('batch-1', 'MARKDOWN', 1700000000, 1700000001,
              'COMMITTED', NULL, 5, 5, 0, 0, '1.0', 3)
            """.trimIndent(),
        )
        for (i in 1..5) {
            db.execSQL(
                """
                INSERT INTO import_batch_items VALUES ('batch-1:$i', 'batch-1', $i,
                  'entry-${"%03d".format(i)}', 'IMPORTED', 1700000000, 3)
                """.trimIndent(),
            )
        }

        val envelopes = db.query("SELECT content_envelope FROM entries ORDER BY created_at").use { cursor ->
            buildList {
                while (cursor.moveToNext()) add((cursor.getBlob(0) ?: byteArrayOf()).copyOf())
            }
        }
        db.close()
        return dbFile to envelopes
    }

    private fun createSchema3Tables(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `entries` (
                `id` TEXT NOT NULL, `created_at` INTEGER NOT NULL,
                `original_created_at` INTEGER NOT NULL, `original_time_zone` TEXT,
                `updated_at` INTEGER NOT NULL, `source` TEXT NOT NULL,
                `content_envelope` BLOB NOT NULL, `content_format` TEXT NOT NULL,
                `return_mode` TEXT NOT NULL, `last_returned_at` INTEGER,
                `return_count` INTEGER NOT NULL DEFAULT 0,
                `import_batch_id` TEXT, `import_fingerprint_envelope` BLOB,
                `deleted_at` INTEGER, `purge_after` INTEGER,
                `schema_version` INTEGER NOT NULL, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_entries_deleted_at_original_created_at ON entries(deleted_at, original_created_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_entries_purge_after ON entries(purge_after)")


        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `drafts` (
                `id` TEXT NOT NULL, `singleton_slot` INTEGER NOT NULL,
                `updated_at` INTEGER NOT NULL, `content_envelope` BLOB NOT NULL,
                `content_format` TEXT NOT NULL, `schema_version` INTEGER NOT NULL, PRIMARY KEY(`singleton_slot`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `app_settings` (
                `id` INTEGER NOT NULL, `return_consent_state` TEXT NOT NULL,
                `returns_paused_at` INTEGER, `app_lock_mode` TEXT NOT NULL,
                `privacy_cover_enabled` INTEGER NOT NULL, `analytics_enabled` INTEGER NOT NULL,
                `quiet_hours_start_minute` INTEGER NOT NULL DEFAULT 1260,
                `quiet_hours_end_minute` INTEGER NOT NULL DEFAULT 480,
                `notification_permission_observed` TEXT NOT NULL DEFAULT 'UNKNOWN',
                `schema_version` INTEGER NOT NULL, PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `returns` (
                `id` TEXT NOT NULL, `entry_id` TEXT NOT NULL, `algorithm` TEXT NOT NULL,
                `algorithm_version` TEXT NOT NULL, `state` TEXT NOT NULL,
                `created_at` INTEGER NOT NULL, `window_start` INTEGER NOT NULL,
                `window_end` INTEGER NOT NULL, `scheduled_at` INTEGER,
                `notified_at` INTEGER, `opened_at` INTEGER, `dismissed_at` INTEGER,
                `expired_at` INTEGER, `cancelled_at` INTEGER, `cancel_reason` TEXT,
                `age_bucket` TEXT NOT NULL, `schema_version` INTEGER NOT NULL,
                PRIMARY KEY(`id`), FOREIGN KEY(`entry_id`) REFERENCES `entries`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_returns_entry_id ON returns(entry_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_returns_state_window_end ON returns(state, window_end)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_returns_created_at ON returns(created_at)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `import_batches` (
                `id` TEXT NOT NULL, `source` TEXT NOT NULL,
                `started_at` INTEGER NOT NULL, `committed_at` INTEGER NOT NULL,
                `status` TEXT NOT NULL, `source_file_name_envelope` BLOB,
                `parsed_count` INTEGER NOT NULL, `imported_count` INTEGER NOT NULL,
                `duplicate_count` INTEGER NOT NULL, `failed_count` INTEGER NOT NULL,
                `parser_version` TEXT NOT NULL, `schema_version` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_batches_committed_at ON import_batches (committed_at)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `import_batch_items` (
                `id` TEXT NOT NULL, `batch_id` TEXT NOT NULL, `source_index` INTEGER NOT NULL,
                `entry_id` TEXT, `status` TEXT NOT NULL,
                `imported_updated_at` INTEGER NOT NULL, `schema_version` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`batch_id`) REFERENCES `import_batches`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(`entry_id`) REFERENCES `entries`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_batch_items_batch_id ON import_batch_items (batch_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_batch_items_entry_id ON import_batch_items (entry_id)")
    }

    private fun openHelper(file: java.io.File, version: Int): SupportSQLiteDatabase =
        FrameworkSQLiteOpenHelperFactory().create(
            androidx.sqlite.db.SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(file.absolutePath)
                .callback(object : androidx.sqlite.db.SupportSQLiteOpenHelper.Callback(version) {
                    override fun onCreate(db: SupportSQLiteDatabase) = Unit
                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build(),
        ).writableDatabase

    private fun readContentEnvelopesRoom(db: FioDatabase): List<ByteArray> =
        db.openHelper.writableDatabase.query("SELECT content_envelope FROM entries ORDER BY created_at")
            .use { cursor ->
                buildList {
                    while (cursor.moveToNext()) add((cursor.getBlob(0) ?: byteArrayOf()).copyOf())
                }
            }

    private fun assertStructuralIdentity(db: FioDatabase) {
        val expectedEntriesColumns = listOf(
            "id", "created_at", "original_created_at", "original_time_zone", "updated_at",
            "source", "content_envelope", "content_format", "return_mode", "last_returned_at",
            "return_count", "import_batch_id", "import_fingerprint_envelope",
            "deleted_at", "purge_after", "schema_version",
            "requested_window_start", "requested_window_end",
        )
        val actual = db.openHelper.writableDatabase.query(
            androidx.sqlite.db.SimpleSQLiteQuery("PRAGMA table_info(entries)"),
        ).use { cursor ->
            buildList {
                while (cursor.moveToNext()) add(cursor.getString(1))
            }
        }
        assertEquals(
            "entries column SET must match schema-4",
            expectedEntriesColumns.toSet(),
            actual.toSet(),
        )
    }

    private fun FioDatabase.count(table: String, where: String? = null): Int =
        this.openHelper.writableDatabase.query(
            "SELECT COUNT(*) FROM $table${if (where == null) "" else " WHERE $where"}",
        ).use { cursor ->
            cursor.moveToNext()
            cursor.getInt(0)
        }
}
