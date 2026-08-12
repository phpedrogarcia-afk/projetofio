package com.projetofio.app

import android.app.Application
import androidx.room.Room
import com.projetofio.app.application.FioService
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.AndroidKeystoreKeyProvider
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.DatabasePreflight
import com.projetofio.app.persistence.RoomFioRepository
import java.time.Clock
import java.util.UUID

class FioApplication : Application() {
    val graph: FioGraph by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { FioGraph(this) }
}

class FioGraph(application: Application) {
    private val clock: Clock = Clock.systemDefaultZone()
    private val databaseName = "fio-v1.db"

    val database: FioDatabase = run {
        DatabasePreflight.verifyExistingDatabase(application, databaseName)
        Room.databaseBuilder(
            application,
            FioDatabase::class.java,
            databaseName,
        ).setJournalMode(androidx.room.RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .build()
    }

    private val keyProvider = AndroidKeystoreKeyProvider {
        database.fioDao().encryptedRecordCount() > 0
    }

    private val repository = RoomFioRepository(
        dao = database.fioDao(),
        cipher = AesGcmContentCipher(keyProvider),
        clock = clock,
    )

    val service = FioService(
        repository = repository,
        clock = clock,
        ids = IdGenerator { UUID.randomUUID().toString() },
    )
}
