package com.projetofio.app

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.projetofio.app.application.FioService
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.ContentKeyProvider
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.RoomFioRepository
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
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
class EncryptedPersistenceTest {
    private lateinit var database: FioDatabase
    private lateinit var service: FioService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, FioDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val generator = KeyGenerator.getInstance("AES").apply { init(256) }
        val cipher = AesGcmContentCipher(FixedProvider(generator.generateKey()))
        val repository = RoomFioRepository(database.fioDao(), cipher, Clock.systemUTC())
        service = FioService(
            repository,
            Clock.fixed(Instant.parse("2026-08-10T12:30:00Z"), ZoneOffset.UTC),
            IdGenerator { "synthetic-entry-id" },
        )
    }

    @After
    fun close() = database.close()

    @Test
    fun persistentPayloadIsCiphertextAndExactContentReopens() = runBlocking {
        val source = "Amostra sintética — çã ☕\nlinha dois"
        val saved = service.saveEntry(source)
        val row = database.fioDao().findEntry(saved.id)!!
        assertFalse(row.contentEnvelope.toString(Charsets.UTF_8).contains(source))
        assertEquals(source, service.observeActiveEntries().first().single().content)
    }

    private class FixedProvider(private val key: SecretKey) : ContentKeyProvider {
        override fun keyForEncryption(): SecretKey = key
        override fun keyForDecryption(): SecretKey = key
    }
}
