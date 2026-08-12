package com.projetofio.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projetofio.app.application.FioService
import com.projetofio.app.application.ExportFormat
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.AndroidKeystoreKeyProvider
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.RoomFioRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeystorePersistenceTest {
    @Test
    fun trustedLocalLifecycleSurvivesDatabaseReopen() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val databaseName = "fio-synthetic-keystore-test.db"
        context.deleteDatabase(databaseName)
        var database = openDatabase(context, databaseName)
        try {
            val cipher = AesGcmContentCipher(
                AndroidKeystoreKeyProvider { database.fioDao().encryptedRecordCount() > 0 },
            )
            val fixedClock = Clock.fixed(Instant.parse("2026-08-10T12:30:00Z"), ZoneOffset.UTC)
            var service = FioService(
                RoomFioRepository(database.fioDao(), cipher, fixedClock),
                fixedClock,
                IdGenerator { "synthetic-keystore-entry" },
            )
            service.autosaveDraft("Rascunho sintético — çã")
            database.close()

            database = openDatabase(context, databaseName)
            val reopenedCipher = AesGcmContentCipher(
                AndroidKeystoreKeyProvider { database.fioDao().encryptedRecordCount() > 0 },
            )
            service = FioService(
                RoomFioRepository(database.fioDao(), reopenedCipher, fixedClock),
                fixedClock,
                IdGenerator { "synthetic-keystore-entry" },
            )
            assertEquals("Rascunho sintético — çã", service.loadDraft()?.content)

            val saved = service.saveEntry("Amostra sintética do Android Keystore — çã")
            assertNull(service.loadDraft())
            database.close()

            database = openDatabase(context, databaseName)
            val finalCipher = AesGcmContentCipher(
                AndroidKeystoreKeyProvider { database.fioDao().encryptedRecordCount() > 0 },
            )
            service = FioService(
                RoomFioRepository(database.fioDao(), finalCipher, fixedClock),
                fixedClock,
                IdGenerator { "unused-synthetic-id" },
            )
            assertEquals(
                "Amostra sintética do Android Keystore — çã",
                service.observeActiveEntries().first().single().content,
            )

            service.editEntry(saved.id, "Amostra sintética editada — ☕")
            assertEquals("Amostra sintética editada — ☕", service.observeActiveEntries().first().single().content)

            val markdown = service.export(ExportFormat.MARKDOWN)
            val plainText = service.export(ExportFormat.PLAIN_TEXT)
            assertTrue(markdown.contains("Amostra sintética editada — ☕"))
            assertTrue(markdown.contains("Tamanho UTF-8:"))
            assertTrue(plainText.contains("Amostra sintética editada — ☕"))
            assertTrue(plainText.contains("FIO: INÍCIO DA ENTRADA"))

            service.moveToRecentlyDeleted(saved.id)
            assertTrue(service.observeActiveEntries().first().isEmpty())
            assertEquals(saved.id, service.observeDeletedEntries().first().single().id)
            service.recoverEntry(saved.id)
            assertEquals(saved.id, service.observeActiveEntries().first().single().id)
            service.moveToRecentlyDeleted(saved.id)
            service.permanentlyDelete(saved.id)
            assertTrue(service.observeDeletedEntries().first().isEmpty())
            assertFalse(database.fioDao().encryptedRecordCount() > 0)
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }

    private fun openDatabase(context: Context, name: String): FioDatabase =
        Room.databaseBuilder(context, FioDatabase::class.java, name).build()
}
