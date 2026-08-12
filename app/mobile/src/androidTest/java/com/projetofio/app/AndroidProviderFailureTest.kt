package com.projetofio.app

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.projetofio.app.application.AndroidDocumentWriter
import com.projetofio.app.application.ExportCoordinator
import com.projetofio.app.application.ExportFormat
import com.projetofio.app.application.ExportOutcome
import java.io.FileNotFoundException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

class FailingExportProvider : ContentProvider() {
    override fun onCreate(): Boolean = true
    override fun getType(uri: Uri): String = "text/plain"
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?,
    ): Cursor? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?,
    ): Int = 0
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        throw FileNotFoundException("synthetic provider refusal")
    }
}

@RunWith(AndroidJUnit4::class)
class AndroidProviderFailureTest {
    @Test
    fun realContentResolverFailureReturnsClosedFailure() = runBlocking {
        val resolver = InstrumentationRegistry.getInstrumentation().context.contentResolver
        val writer = AndroidDocumentWriter(resolver)
        val outcome = ExportCoordinator().export(
            destination = Uri.parse("content://com.projetofio.app.test.failing-export/document"),
            format = ExportFormat.PLAIN_TEXT,
            buildDocument = { "conteúdo sintético privado" },
            writeDocument = writer::write,
        )

        assertEquals(ExportOutcome.FAILED, outcome)
    }
}
