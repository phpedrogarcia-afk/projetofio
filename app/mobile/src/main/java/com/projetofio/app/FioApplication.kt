package com.projetofio.app

import android.app.Application
import androidx.room.Room
import com.projetofio.app.application.FioService
import com.projetofio.app.application.TimeReturnsService
import com.projetofio.app.application.ImportService
import com.projetofio.app.crypto.AesGcmContentCipher
import com.projetofio.app.crypto.AndroidKeystoreKeyProvider
import com.projetofio.app.domain.IdGenerator
import com.projetofio.app.domain.ReturnRandom
import com.projetofio.app.domain.TimeReturnEngine
import com.projetofio.app.domain.LocalImportParser
import com.projetofio.app.domain.SearchService
import com.projetofio.app.persistence.FioDatabase
import com.projetofio.app.persistence.DatabasePreflight
import com.projetofio.app.persistence.MIGRATION_1_2
import com.projetofio.app.persistence.MIGRATION_2_3
import com.projetofio.app.persistence.MIGRATION_3_4
import com.projetofio.app.persistence.RoomFioRepository
import com.projetofio.app.search.LocalSearchService
import java.time.Clock
import java.util.UUID
import java.security.SecureRandom
import com.projetofio.app.returns.AndroidReturnNotifications
import com.projetofio.app.returns.WorkManagerReturnScheduler

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
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
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

    val timeReturns = TimeReturnsService(
        entries = repository,
        returns = repository,
        engine = TimeReturnEngine(ReturnRandom { bound -> SecureRandom().nextInt(bound) }),
        scheduler = WorkManagerReturnScheduler(application, clock),
        notifications = AndroidReturnNotifications(application),
        clock = clock,
        ids = IdGenerator { UUID.randomUUID().toString() },
        engineeringEnabled = BuildConfig.TIME_RETURNS_ENGINEERING_ENABLED,
    )

    val search: SearchService = LocalSearchService(
        repository = repository,
    )

    val localImport = ImportService(
        entries = repository,
        imports = repository,
        timeReturns = timeReturns,
        parser = LocalImportParser(),
        clock = clock,
        ids = IdGenerator { UUID.randomUUID().toString() },
        engineeringEnabled = BuildConfig.LOCAL_IMPORT_ENGINEERING_ENABLED,
    )
}
