package com.projetofio.app.application

import kotlinx.coroutines.CancellationException

enum class ExportOutcome { SUCCESS, CANCELLED, FAILED }

class ExportCoordinator {
    suspend fun <Destination> export(
        destination: Destination?,
        format: ExportFormat,
        buildDocument: suspend (ExportFormat) -> String,
        writeDocument: suspend (Destination, String) -> Unit,
    ): ExportOutcome {
        if (destination == null) return ExportOutcome.CANCELLED
        return try {
            val document = buildDocument(format)
            writeDocument(destination, document)
            ExportOutcome.SUCCESS
        } catch (error: CancellationException) {
            throw error
        } catch (_: Exception) {
            ExportOutcome.FAILED
        }
    }
}
