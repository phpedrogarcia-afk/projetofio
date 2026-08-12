package com.projetofio.app.application

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExportCoordinatorTest {
    private val coordinator = ExportCoordinator()

    @Test
    fun cancellationDoesNotBuildOrWritePlaintext() = runBlocking {
        var built = false
        var written = false

        val result = coordinator.export<String>(
            destination = null,
            format = ExportFormat.MARKDOWN,
            buildDocument = {
                built = true
                "private synthetic text"
            },
            writeDocument = { _, _ -> written = true },
        )

        assertEquals(ExportOutcome.CANCELLED, result)
        assertFalse(built)
        assertFalse(written)
    }

    @Test
    fun providerFailureReturnsClosedFailureWithoutRetainedCopy() = runBlocking {
        var observedDocument: String? = null

        val result = coordinator.export(
            destination = "synthetic-provider",
            format = ExportFormat.PLAIN_TEXT,
            buildDocument = { "conteúdo sintético" },
            writeDocument = { _, document ->
                observedDocument = document
                error("synthetic provider failure")
            },
        )

        assertEquals(ExportOutcome.FAILED, result)
        assertEquals("conteúdo sintético", observedDocument)
    }

    @Test
    fun largeUnicodeDocumentReachesDestinationExactly() = runBlocking {
        val source = buildString {
            repeat(20_000) { append("linha $it — çã ☕  \n") }
        }
        var written = ""

        val result = coordinator.export(
            destination = "synthetic-provider",
            format = ExportFormat.MARKDOWN,
            buildDocument = { source },
            writeDocument = { _, document -> written = document },
        )

        assertEquals(ExportOutcome.SUCCESS, result)
        assertTrue(written.length > 400_000)
        assertEquals(source, written)
    }
}
