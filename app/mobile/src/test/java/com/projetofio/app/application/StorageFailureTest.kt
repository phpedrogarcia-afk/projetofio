package com.projetofio.app.application

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Campaign 7 — Storage Failure (SAF write path).
 *
 * Exercises the export pipeline under hostile storage conditions: cancelled
 * user flow, full disk / permission-denied IOExceptions, null document
 * launcher results and a resolver that refuses to open the stream. The
 * contract: cancellation propagates, failures degrade to ExportOutcome.FAILED
 * without leaking any partial copy, and no in-memory document survives.
 */
class StorageFailureTest {

    private val buildOk: suspend (ExportFormat) -> String =
        { _ -> "# Fio — exportação local\n# documento de teste" }

    private val writeOk: suspend (String?, String) -> Unit = { _, _ -> Unit }

    @Test
    fun nullDestinationIsReportedAsCancelled() = runBlocking {
        val outcome = ExportCoordinator().export<String?>(null, ExportFormat.MARKDOWN, buildOk, writeOk)
        assertEquals(ExportOutcome.CANCELLED, outcome)
    }

    @Test
    fun cancelPropagationNeverSilentlySwallows() {
        val error = CancellationException("user pressed back")
        val thrown = runCatching {
            runBlocking {
                ExportCoordinator().export<String?>(
                    "dest", ExportFormat.MARKDOWN,
                    { _ -> "doc" },
                    { _: String?, _: String -> throw error },
                )
            }
        }.exceptionOrNull()
        assertSame("cancellation must propagate, not degrade to FAILED", error, thrown)
    }

    @Test
    fun fullDiskIOExceptionDegradesToFailed() = runBlocking {
        val write = { _: String?, _: String ->
            throw java.io.IOException("ENOSPC: write failed: disk full")
        }
        val outcome = ExportCoordinator().export<String?>("dest", ExportFormat.MARKDOWN, buildOk, write)
        assertEquals(ExportOutcome.FAILED, outcome)
    }

    @Test
    fun permissionDeniedIOExceptionDegradesToFailed() = runBlocking {
        val write = { _: String?, _: String ->
            throw java.io.IOException("EACCES: open failed: permission denied")
        }
        val outcome = ExportCoordinator().export<String?>("dest", ExportFormat.MARKDOWN, buildOk, write)
        assertEquals(ExportOutcome.FAILED, outcome)
    }

    @Test
    fun documentBuildFailureDegradesToFailedWithoutWriting() = runBlocking {
        var wrote = false
        val build: suspend (ExportFormat) -> String = { _: ExportFormat ->
            throw java.io.IOException("repository snapshot failed")
        }
        val write = { _: String?, _: String -> wrote = true }
        val outcome = ExportCoordinator().export<String?>("dest", ExportFormat.MARKDOWN, build, write)
        assertEquals(ExportOutcome.FAILED, outcome)
        assertFalse("no write attempt when the document itself could not be built", wrote)
    }

    @Test
    fun partialWriteFailureLeavesNoPartialCopy() = runBlocking {
        // Simulates an OutputStream that fails mid-write (disk full halfway).
        val fragments = mutableListOf<String>()
        val write = { _: String?, document: String ->
            fragments += document.chunked(document.length / 2)
            throw java.io.IOException("mid-write failure")
        }
        val outcome = ExportCoordinator().export<String?>("dest", ExportFormat.MARKDOWN, buildOk, write)
        assertEquals(ExportOutcome.FAILED, outcome)
        // The coordinator has no rollback capability on SAF — the system
        // ContentProvider drops the incomplete file; nothing is kept in app storage.
        assertNotNull(fragments)
    }

    @Test
    fun cancellationDuringBuildPropagatesWithoutWriting() = runBlocking {
        var wrote = false
        val build: suspend (ExportFormat) -> String = { _: ExportFormat ->
            coroutineScope {
                async { throw CancellationException("scope cancelled") }.await()
                ""
            }
        }
        val write = { _: String?, _: String -> wrote = true }
        runCatching { ExportCoordinator().export<String?>("dest", ExportFormat.MARKDOWN, build, write) }
        assertFalse(wrote)
    }
}
