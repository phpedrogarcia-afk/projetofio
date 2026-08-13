package com.projetofio.app

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.MIGRATION_1_2
import com.projetofio.app.persistence.MIGRATION_2_3
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomSchemaTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        FioDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun versionOneMigratesThroughThreeWithoutConsentReturnOrImport() {
        helper.createDatabase(TEST_DATABASE, 1).apply {
            execSQL(
                "INSERT INTO entries (id, created_at, original_created_at, original_time_zone, updated_at, source, content_envelope, content_format, return_mode, deleted_at, purge_after, schema_version) " +
                    "VALUES ('synthetic-entry', 1000, 1000, 'UTC', 1000, 'NATIVE', X'0102', 'PLAIN_TEXT', 'ELIGIBLE', NULL, NULL, 1)",
            )
            execSQL(
                "INSERT INTO drafts (id, singleton_slot, updated_at, content_envelope, content_format, schema_version) " +
                    "VALUES ('synthetic-draft', 1, 1000, X'0304', 'PLAIN_TEXT', 1)",
            )
            execSQL(
                "INSERT INTO app_settings (id, return_consent_state, returns_paused_at, app_lock_mode, privacy_cover_enabled, analytics_enabled, schema_version) " +
                    "VALUES (1, 'NOT_CONFIGURED', NULL, 'OFF', 1, 0, 1)",
            )
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DATABASE, 3, true, MIGRATION_1_2, MIGRATION_2_3)
        migrated.query("SELECT COUNT(*), last_returned_at, return_count, import_batch_id, import_fingerprint_envelope FROM entries").use {
            assertEquals(true, it.moveToFirst())
            assertEquals(1, it.getInt(0))
            assertEquals(true, it.isNull(1))
            assertEquals(0, it.getInt(2))
            assertEquals(true, it.isNull(3))
            assertEquals(true, it.isNull(4))
        }
        migrated.query("SELECT return_consent_state, quiet_hours_start_minute, quiet_hours_end_minute, notification_permission_observed FROM app_settings").use {
            assertEquals(true, it.moveToFirst())
            assertEquals("NOT_CONFIGURED", it.getString(0))
            assertEquals(1260, it.getInt(1))
            assertEquals(480, it.getInt(2))
            assertEquals("UNKNOWN", it.getString(3))
        }
        migrated.query("SELECT COUNT(*) FROM returns").use {
            assertEquals(true, it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM drafts").use {
            assertEquals(true, it.moveToFirst())
            assertEquals(1, it.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM import_batches").use {
            assertEquals(true, it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
        migrated.query("SELECT COUNT(*) FROM import_batch_items").use {
            assertEquals(true, it.moveToFirst())
            assertEquals(0, it.getInt(0))
        }
        migrated.close()
    }

    @Test
    fun versionTwoMigratesToThreeWithoutChangingM2Rows() {
        helper.createDatabase(SECOND_DATABASE, 2).apply {
            execSQL(
                "INSERT INTO entries (id, created_at, original_created_at, original_time_zone, updated_at, source, content_envelope, content_format, return_mode, last_returned_at, return_count, deleted_at, purge_after, schema_version) " +
                    "VALUES ('m2-entry', 1000, 900, 'UTC', 1100, 'NATIVE', X'010203', 'PLAIN_TEXT', 'ELIGIBLE', 1050, 2, NULL, NULL, 2)",
            )
            execSQL(
                "INSERT INTO app_settings (id, return_consent_state, returns_paused_at, app_lock_mode, privacy_cover_enabled, analytics_enabled, quiet_hours_start_minute, quiet_hours_end_minute, notification_permission_observed, schema_version) " +
                    "VALUES (1, 'ENABLED', NULL, 'OFF', 1, 0, 1320, 540, 'DENIED', 2)",
            )
            close()
        }
        val migrated = helper.runMigrationsAndValidate(SECOND_DATABASE, 3, true, MIGRATION_2_3)
        migrated.query("SELECT original_created_at, updated_at, last_returned_at, return_count, hex(content_envelope), import_batch_id FROM entries WHERE id = 'm2-entry'").use {
            assertEquals(true, it.moveToFirst())
            assertEquals(900L, it.getLong(0))
            assertEquals(1100L, it.getLong(1))
            assertEquals(1050L, it.getLong(2))
            assertEquals(2, it.getInt(3))
            assertEquals("010203", it.getString(4))
            assertEquals(true, it.isNull(5))
        }
        migrated.query("SELECT return_consent_state, quiet_hours_start_minute, quiet_hours_end_minute, notification_permission_observed FROM app_settings").use {
            assertEquals(true, it.moveToFirst())
            assertEquals("ENABLED", it.getString(0))
            assertEquals(1320, it.getInt(1))
            assertEquals(540, it.getInt(2))
            assertEquals("DENIED", it.getString(3))
        }
        migrated.close()
    }

    private companion object {
        const val TEST_DATABASE = "fio-synthetic-migration-test"
        const val SECOND_DATABASE = "fio-synthetic-migration-two-test"
    }
}
