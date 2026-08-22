package com.projetofio.app.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        EntryEntity::class,
        DraftEntity::class,
        AppSettingsEntity::class,
        ReturnEntity::class,
        ImportBatchEntity::class,
        ImportBatchItemEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class FioDatabase : RoomDatabase() {
    abstract fun fioDao(): FioDao
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN last_returned_at INTEGER")
        db.execSQL("ALTER TABLE entries ADD COLUMN return_count INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE app_settings ADD COLUMN quiet_hours_start_minute INTEGER NOT NULL DEFAULT 1260")
        db.execSQL("ALTER TABLE app_settings ADD COLUMN quiet_hours_end_minute INTEGER NOT NULL DEFAULT 480")
        db.execSQL("ALTER TABLE app_settings ADD COLUMN notification_permission_observed TEXT NOT NULL DEFAULT 'UNKNOWN'")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS returns (
                id TEXT NOT NULL PRIMARY KEY,
                entry_id TEXT NOT NULL,
                algorithm TEXT NOT NULL,
                algorithm_version TEXT NOT NULL,
                state TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                window_start INTEGER NOT NULL,
                window_end INTEGER NOT NULL,
                scheduled_at INTEGER,
                notified_at INTEGER,
                opened_at INTEGER,
                dismissed_at INTEGER,
                expired_at INTEGER,
                cancelled_at INTEGER,
                cancel_reason TEXT,
                age_bucket TEXT NOT NULL,
                schema_version INTEGER NOT NULL,
                FOREIGN KEY(entry_id) REFERENCES entries(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_returns_entry_id ON returns(entry_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_returns_state_window_end ON returns(state, window_end)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_returns_created_at ON returns(created_at)")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN import_batch_id TEXT")
        db.execSQL("ALTER TABLE entries ADD COLUMN import_fingerprint_envelope BLOB")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_entries_deleted_at_original_created_at ON entries(deleted_at, original_created_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_entries_purge_after ON entries(purge_after)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS import_batches (
                id TEXT NOT NULL PRIMARY KEY,
                source TEXT NOT NULL,
                started_at INTEGER NOT NULL,
                committed_at INTEGER NOT NULL,
                status TEXT NOT NULL,
                source_file_name_envelope BLOB,
                parsed_count INTEGER NOT NULL,
                imported_count INTEGER NOT NULL,
                duplicate_count INTEGER NOT NULL,
                failed_count INTEGER NOT NULL,
                parser_version TEXT NOT NULL,
                schema_version INTEGER NOT NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_batches_committed_at ON import_batches(committed_at)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS import_batch_items (
                id TEXT NOT NULL PRIMARY KEY,
                batch_id TEXT NOT NULL,
                source_index INTEGER NOT NULL,
                entry_id TEXT,
                status TEXT NOT NULL,
                imported_updated_at INTEGER NOT NULL,
                schema_version INTEGER NOT NULL,
                FOREIGN KEY(batch_id) REFERENCES import_batches(id) ON UPDATE NO ACTION ON DELETE CASCADE,
                FOREIGN KEY(entry_id) REFERENCES entries(id) ON UPDATE NO ACTION ON DELETE SET NULL
            )
            """.trimIndent(),
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_batch_items_batch_id ON import_batch_items(batch_id)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_import_batch_items_entry_id ON import_batch_items(entry_id)")
    }
}

// FIO-P19 A1: additive migration — two nullable columns for the requested delivery window.
// Existing entries get NULL in both fields; they remain ELIGIBLE and are drawn from the organic pool.
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE entries ADD COLUMN requested_window_start INTEGER")
        db.execSQL("ALTER TABLE entries ADD COLUMN requested_window_end INTEGER")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_entries_deleted_at_original_created_at ON entries(deleted_at, original_created_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_entries_purge_after ON entries(purge_after)")
    }
}

