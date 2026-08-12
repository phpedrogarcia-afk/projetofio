package com.projetofio.app.application

import android.content.ContentResolver
import android.net.Uri
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidDocumentWriter(
    private val contentResolver: ContentResolver,
) {
    suspend fun write(destination: Uri, document: String) {
        withContext(Dispatchers.IO) {
            val stream = checkNotNull(contentResolver.openOutputStream(destination, "wt"))
            stream.bufferedWriter(StandardCharsets.UTF_8).use { writer ->
                writer.write(document)
            }
        }
    }
}
