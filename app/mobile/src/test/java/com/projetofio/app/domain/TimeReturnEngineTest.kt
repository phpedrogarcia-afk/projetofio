package com.projetofio.app.domain

import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeReturnEngineTest {
    private val zone = ZoneId.of("America/Sao_Paulo")
    private val now = Instant.parse("2026-08-13T15:00:00Z")

    @Test
    fun consentIsAnAbsoluteGate() {
        val entries = listOf(entry("old", 40))
        for (state in listOf(ReturnConsentState.NOT_CONFIGURED, ReturnConsentState.PAUSED)) {
            val settings = AppSettings(
                returnConsentState = state,
                returnsPausedAt = if (state == ReturnConsentState.PAUSED) now else null,
            )
            assertEquals(
                TimeReturnDecision.Silent(SilenceReason.CONSENT_DISABLED),
                engine().evaluate(now, zone, settings, entries, emptyList()),
            )
        }
    }

    @Test
    fun bootstrapRequiresAgeBasedOnHistorySize() {
        val enabled = AppSettings(returnConsentState = ReturnConsentState.ENABLED)
        assertEquals(
            SilenceReason.BOOTSTRAP_WAIT,
            (engine().evaluate(now, zone, enabled, listOf(entry("one", 29)), emptyList()) as TimeReturnDecision.Silent).reason,
        )
        assertEquals(
            SilenceReason.BOOTSTRAP_WAIT,
            (engine().evaluate(now, zone, enabled, listOf(entry("a", 13), entry("b", 10)), emptyList()) as TimeReturnDecision.Silent).reason,
        )
        assertTrue(
            engine().evaluate(
                now,
                zone,
                enabled,
                listOf(entry("a", 7), entry("b", 7), entry("c", 7), entry("d", 7)),
                emptyList(),
            ) is TimeReturnDecision.Selected,
        )
    }

    @Test
    fun pendingReturnAndRollingCapStaySilent() {
        val enabled = AppSettings(returnConsentState = ReturnConsentState.ENABLED)
        val entries = listOf(entry("old", 40))
        assertEquals(
            SilenceReason.PENDING_RETURN,
            (engine().evaluate(now, zone, enabled, entries, listOf(attempt("pending", ReturnState.SCHEDULED, 9))) as TimeReturnDecision.Silent).reason,
        )
        assertEquals(
            SilenceReason.FREQUENCY_CAP,
            (engine().evaluate(now, zone, enabled, entries, listOf(attempt("other", ReturnState.DISMISSED, 2))) as TimeReturnDecision.Silent).reason,
        )
    }

    @Test
    fun entryIsNeverSelectedTwiceDuringV0() {
        val decision = engine().evaluate(
            now,
            zone,
            AppSettings(returnConsentState = ReturnConsentState.ENABLED),
            listOf(entry("used", 100)),
            listOf(attempt("used", ReturnState.DISMISSED, 20)),
        )
        assertEquals(TimeReturnDecision.Silent(SilenceReason.NO_ELIGIBLE_ENTRY), decision)
    }

    @Test
    fun deterministicSelectionUsesCanonicalAgeBucket() {
        val decision = engine(1).evaluate(
            now,
            zone,
            AppSettings(returnConsentState = ReturnConsentState.ENABLED),
            listOf(entry("a", 40), entry("b", 100), entry("c", 100)),
            emptyList(),
        ) as TimeReturnDecision.Selected
        assertEquals(AgeBucket.DAYS_90_179, decision.ageBucket)
        assertEquals("c", decision.entryId)
    }

    @Test
    fun allAgeBucketBoundariesAreCanonical() {
        val ages = listOf(7L, 30L, 90L, 180L, 365L, 730L)
        val expected = AgeBucket.entries
        ages.indices.forEach { index ->
            val decision = engine(index).evaluate(
                now,
                zone,
                AppSettings(returnConsentState = ReturnConsentState.ENABLED),
                ages.map { age -> entry("age-$age", age) },
                emptyList(),
            ) as TimeReturnDecision.Selected
            assertEquals(expected[index], decision.ageBucket)
        }
    }

    @Test
    fun defaultQuietHoursMoveNightOpportunityToEightAm() {
        val night = Instant.parse("2026-08-14T03:00:00Z") // midnight in Sao Paulo
        val decision = engine(0).evaluate(
            night,
            zone,
            AppSettings(returnConsentState = ReturnConsentState.ENABLED),
            listOf(entryAt("old", night.minusSeconds(40L * 86_400))),
            emptyList(),
        ) as TimeReturnDecision.Selected
        assertEquals("2026-08-14T11:00:00Z", decision.deliveryAt.toString())
    }

    @Test
    fun dstGapMovesQuietEndToFirstValidLocalInstant() {
        val newYork = ZoneId.of("America/New_York")
        val gapNight = Instant.parse("2026-03-08T06:30:00Z")
        val decision = engine().evaluate(
            gapNight,
            newYork,
            AppSettings(
                returnConsentState = ReturnConsentState.ENABLED,
                quietHoursStartMinute = 21 * 60,
                quietHoursEndMinute = 2 * 60,
            ),
            listOf(entryAt("old", gapNight.minusSeconds(40L * 86_400))),
            emptyList(),
        ) as TimeReturnDecision.Selected
        assertEquals("2026-03-08T07:00:00Z", decision.deliveryAt.toString())
    }

    @Test
    fun dstOverlapUsesAValidDeterministicQuietEnd() {
        val newYork = ZoneId.of("America/New_York")
        val overlapNight = Instant.parse("2026-11-01T04:30:00Z")
        val decision = engine().evaluate(
            overlapNight,
            newYork,
            AppSettings(
                returnConsentState = ReturnConsentState.ENABLED,
                quietHoursStartMinute = 21 * 60,
                quietHoursEndMinute = 90,
            ),
            listOf(entryAt("old", overlapNight.minusSeconds(40L * 86_400))),
            emptyList(),
        ) as TimeReturnDecision.Selected
        assertEquals("2026-11-01T05:30:00Z", decision.deliveryAt.toString())
    }

    @Test
    fun deletedAndNeverEntriesAreExcluded() {
        val enabled = AppSettings(returnConsentState = ReturnConsentState.ENABLED)
        val deleted = entry("deleted", 50).copy(deletedAt = now, purgeAfter = now.plusSeconds(1))
        val never = entry("never", 50).copy(returnMode = ReturnMode.NEVER)
        val decision = engine().evaluate(now, zone, enabled, listOf(deleted, never), emptyList())
        assertEquals(TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT), decision)
    }

    private fun engine(value: Int = 0) = TimeReturnEngine(ReturnRandom { value })

    private fun entry(id: String, ageDays: Long) = entryAt(id, now.minusSeconds(ageDays * 86_400))

    private fun entryAt(id: String, created: Instant) = Entry(
        id = id,
        createdAt = created,
        originalCreatedAt = created,
        originalTimeZone = zone.id,
        updatedAt = created,
        content = "conteúdo sintético $id",
    )

    private fun attempt(entryId: String, state: ReturnState, ageDays: Long) = ReturnAttempt(
        id = "return-$entryId-$ageDays",
        entryId = entryId,
        state = state,
        createdAt = now.minusSeconds(ageDays * 86_400),
        windowStart = now.minusSeconds(ageDays * 86_400),
        windowEnd = now.minusSeconds(ageDays * 86_400).plusSeconds(86_400),
        ageBucket = AgeBucket.DAYS_30_89,
    )
}
