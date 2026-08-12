package com.projetofio.app.persistence

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase

class UnsafeDatabaseOpen(cause: Throwable? = null) : IllegalStateException(
    "The local database could not be opened without risking destructive recovery.",
    cause,
)

object DatabasePreflight {
    fun verifyExistingDatabase(context: Context, databaseName: String) {
        val file = context.getDatabasePath(databaseName)
        if (!file.exists() || file.length() == 0L) return

        var database: SQLiteDatabase? = null
        try {
            database = SQLiteDatabase.openDatabase(
                file.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
                DatabaseErrorHandler { throw UnsafeDatabaseOpen() },
            )
            database.rawQuery("PRAGMA quick_check(1)", null).use { cursor ->
                if (!cursor.moveToFirst() || cursor.getString(0) != "ok") {
                    throw UnsafeDatabaseOpen()
                }
            }
        } catch (error: UnsafeDatabaseOpen) {
            throw error
        } catch (error: Exception) {
            throw UnsafeDatabaseOpen(error)
        } finally {
            database?.close()
        }
    }
}
