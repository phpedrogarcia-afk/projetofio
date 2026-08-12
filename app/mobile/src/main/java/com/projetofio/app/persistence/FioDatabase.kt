package com.projetofio.app.persistence

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [EntryEntity::class, DraftEntity::class, AppSettingsEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class FioDatabase : RoomDatabase() {
    abstract fun fioDao(): FioDao
}
