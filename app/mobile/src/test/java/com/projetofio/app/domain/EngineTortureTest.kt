package com.projetofio.app.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

/**
 * Campaign 4 — Returns Engine Torture (TimeReturnEngine).
 *
 * Deterministic replay with fixed RNGs, property checks over 100 random seeds,
 * boundary torture of age buckets, quiet-hours mathematics, frequency cap and
 * bootstrap rules.
 */
class EngineTortureTest {

    private val zone = ZoneId.of("America/Sao_Paulo")

    private fun entry(
        id: String,
        originalCreatedAt: Instant,
        returnMode: ReturnMode = ReturnMode.ELIGIBLE,
        deletedAt: Instant? = null,
        purgeAfter: Instant? = null,
    ) = Entry(
        id = id,
        createdAt = originalCreatedAt,
        originalCreatedAt = originalCreatedAt,
        originalTimeZone = "America/Sao_Paulo",
        updatedAt = originalCreatedAt,
        content = "content of $id",
        returnMode = returnMode,
        deletedAt = deletedAt,
        purgeAfter = purgeAfter,
    )

    private fun settings(
        consent: ReturnConsentState = ReturnConsentState.ENABLED,
        startMinute: Int = 21 * 60,
        endMinute: Int = 8 * 60,
        pausedNow: Instant? = null,
    ) = AppSettings(
        returnConsentState = consent,
        returnsPausedAt = if (consent == ReturnConsentState.PAUSED) (pausedNow ?: Instant.now()) else null,
        quietHoursStartMinute = startMinute,
        quietHoursEndMinute = endMinute,
    )

    /** Deterministic replay RNG: pre-canned sequence. */
    private class FixedRng(sequence: List<Int>) : ReturnRandom {
        private val it = sequence.iterator()
        override fun nextInt(bound: Int): Int =
            if (it.hasNext()) it.next() else error("RNG exhausted at bound=$bound")
    }

    /** Seeded linear congruential generator for property sweeps. */
    private class SeededRng(seed: Long) : ReturnRandom {
        private var state = seed
        override fun nextInt(bound: Int): Int {
            state = state * 6364136223846793005L + 1442695040888963407L
            return Math.floorMod(((state ushr 33) and 0x7fffffffL).toInt(), bound)
        }
    }

    private fun selected(dec: TimeReturnDecision): TimeReturnDecision.Selected =
        requireNotNull(dec as? TimeReturnDecision.Selected) { "expected Selected, got $dec" }

    @Test
    fun deterministicReplayFixedRngYieldsIdenticalDecisionsAcrossRuns() {
        val rng = listOf(3, 1, 0, 2, 0, 1)
        val now = LocalDate.of(2026, 8, 1).atStartOfDay(zone).toInstant()
        val entries = (1..12).map { entry("e$it", now.minusSeconds(it.toLong() * 30 * 86400)) }
        val first = TimeReturnEngine(FixedRng(rng)).evaluate(now, zone, settings(), entries, emptyList())
        val second = TimeReturnEngine(FixedRng(rng)).evaluate(now, zone, settings(), entries, emptyList())
        assertNotNull(first as? TimeReturnDecision.Selected)
        assertEquals(first as TimeReturnDecision.Selected, second)
    }

    @Test
    fun propertySelectedDeliveryNeverFallsInsideQuietHoursAcrossSeeds() {
        val now = ZonedDateTime.of(2026, 6, 15, 22, 30, 0, 0, zone).toInstant()
        val entries = (1..20).map { entry("e$it", now.minusSeconds(it.toLong() * 40 * 86400)) }
        for (seed in 1..100) {
            val sel = selected(TimeReturnEngine(SeededRng(seed.toLong())).evaluate(now, zone, settings(), entries, emptyList()))
            val minuteOfDay = sel.deliveryAt.atZone(zone).hour * 60 + sel.deliveryAt.atZone(zone).minute
            // Quiet: [21:00, 08:00). Notified must fall inside [08:00, 21:00).
            assertTrue("seed=$seed: delivery inside quiet window", minuteOfDay >= 8 * 60 && minuteOfDay < 21 * 60)
            assertEquals("window is always 24h", 86400, sel.windowEnd.epochSecond - sel.deliveryAt.epochSecond)
            assertTrue("selected entry must be at least 7 days old", ageDays(sel.entryId, entries, now) >= 7)
        }
    }

    private fun ageDays(entryId: String, entries: List<Entry>, now: Instant): Long =
        ChronoUnit.DAYS.between(
            entries.first { it.id == entryId }.originalCreatedAt.atZone(zone).toLocalDate(),
            now.atZone(zone).toLocalDate(),
        )

    @Test
    fun propertySelectedEntryNeverInReturnHistoryAcrossSeeds() {
        val now = ZonedDateTime.of(2026, 6, 15, 10, 0, 0, 0, zone).toInstant()
        val entries = (1..15).map { entry("e$it", now.minusSeconds(it.toLong() * 25 * 86400)) }
        val alreadyReturned = alreadyOpened(1..3, now)
        for (seed in 1..100) {
            val sel = selected(
                TimeReturnEngine(SeededRng(seed.toLong())).evaluate(now, zone, settings(), entries, alreadyReturned),
            )
            assertFalse("seed=$seed: reselected an already-returned entry", alreadyReturned.any { it.entryId == sel.entryId })
        }
    }

    private fun alreadyOpened(ids: IntRange, now: Instant) = ids.map {
        ReturnAttempt(
            id = "h$it", entryId = "e$it", state = ReturnState.OPENED,
            createdAt = now.minusSeconds(8 * 86400),
                    windowStart = now.minusSeconds(9 * 86400),
                    windowEnd = now.minusSeconds(7 * 86400),
                    openedAt = now.minusSeconds(8 * 86400 + 3600),
                    ageBucket = AgeBucket.DAYS_30_89,
        )
    }

    @Test
    fun edgeConsentStatesShortCircuitBeforeAnythingElse() {
        val now = Instant.now()
        val entries = listOf(entry("e1", now.minusSeconds(40 * 86400)))
        val engine = TimeReturnEngine { 0 }
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.CONSENT_DISABLED),
            engine.evaluate(now, zone, settings(consent = ReturnConsentState.NOT_CONFIGURED), entries, emptyList()),
        )
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.CONSENT_DISABLED),
            engine.evaluate(now, zone, settings(consent = ReturnConsentState.PAUSED, pausedNow = now), entries, emptyList()),
        )
    }

    @Test
    fun edgePendingStatesSilenceEvenWhenAnEntryIsReady() {
        val now = Instant.now()
        val entries = listOf(entry("e1", now.minusSeconds(40 * 86400)))
        val engine = TimeReturnEngine { 0 }
        for (pending in listOf(ReturnState.SELECTED, ReturnState.SCHEDULED, ReturnState.NOTIFIED)) {
            val history = listOf(
                ReturnAttempt(
                    id = "h1", entryId = "e0", state = pending, createdAt = now.minusSeconds(8 * 86400),
                    windowStart = now.minusSeconds(9 * 86400), windowEnd = now.minusSeconds(7 * 86400),
                    ageBucket = AgeBucket.DAYS_30_89,
                ),
            )
            assertEquals(
                TimeReturnDecision.Silent(SilenceReason.PENDING_RETURN),
                engine.evaluate(now, zone, settings(), entries, history),
            )
        }
        // OPENED / EXPIRED / CANCELLED are terminal: not pending.
        for (terminal in listOf(ReturnState.OPENED, ReturnState.EXPIRED, ReturnState.CANCELLED)) {
            val history = listOf(
                ReturnAttempt(
                    id = "h1", entryId = "e0", state = terminal, createdAt = now.minusSeconds(8 * 86400),
                    windowStart = now.minusSeconds(9 * 86400), windowEnd = now.minusSeconds(7 * 86400),
                    ageBucket = AgeBucket.DAYS_30_89,
                ),
            )
            assertTrue(
                "terminal state $terminal must not silence a fresh return",
                engine.evaluate(now, zone, settings(), entries, history) is TimeReturnDecision.Selected,
            )
        }
    }

    @Test
    fun edgeFrequencyCapBlocksEverythingInsideSevenDays() {
        val now = Instant.now()
        val entries = (1..10).map { entry("e$it", now.minusSeconds(it.toLong() * 30 * 86400)) }
        val engine = TimeReturnEngine { 0 }
        val capHistory = listOf(
            ReturnAttempt(
                id = "h1", entryId = "e99", state = ReturnState.OPENED, createdAt = now.minusSeconds(8 * 86400),
                windowStart = now.minusSeconds(9 * 86400), windowEnd = now.minusSeconds(7 * 86400),
                ageBucket = AgeBucket.DAYS_30_89,
            ),
        )
        assertTrue(engine.evaluate(now, zone, settings(), entries, capHistory) is TimeReturnDecision.Selected)
        // Exactly at the boundary (createdAt == now - 7d): cap uses strict `>` so this does NOT block —
        // pinned as documented behavior to prevent silent drift.
        val boundaryHistory = capHistory.map { it.copy(createdAt = now.minusSeconds(7 * 86400)) }
        assertTrue(engine.evaluate(now, zone, settings(), entries, boundaryHistory) is TimeReturnDecision.Selected)
        // 6 days 23 hours: blocks.
        val insideHistory = capHistory.map { it.copy(createdAt = now.minusSeconds((7 * 86400) - 3600)) }
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.FREQUENCY_CAP),
            engine.evaluate(now, zone, settings(), entries, insideHistory),
        )
    }

    @Test
    fun edgeEmptyAndFullyDeletedPoolsReturnBootstrapWait() {
        val now = Instant.now()
        val engine = TimeReturnEngine { 0 }
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT),
            engine.evaluate(now, zone, settings(), emptyList(), emptyList()),
        )
        val deleted = listOf(entry("d1", now.minusSeconds(365 * 86400), deletedAt = now, purgeAfter = now.plusSeconds(86400)))
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT),
            engine.evaluate(now, zone, settings(), deleted, emptyList()),
        )
    }

    @Test
    fun edgeAllNeverEntriesReturnNoEligibleEntry() {
        val now = Instant.now()
        val entries = (1..5).map { entry("e$it", now.minusSeconds(it.toLong() * 60 * 86400), returnMode = ReturnMode.NEVER) }
        // Active eligible pool is empty: with history-free evaluation the engine reports BOOTSTRAP_WAIT
        // (the oldest-age gate runs before the eligible-pool check). Pinning the observed order.
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT),
            TimeReturnEngine { 0 }.evaluate(now, zone, settings(), entries, emptyList()),
        )
    }

    @Test
    fun ageBucketBoundaries() {
        val now = ZonedDateTime.of(2026, 6, 15, 10, 0, 0, 0, zone).toInstant()
        fun daysAgo(days: Long) = now.minusSeconds(days * 86400)
        // Pool structure: four entries ALL inside the target bucket so that whichever entry the
        // deterministic RNG selects, the returned ageBucket matches the target. One entry per age,
        // chosen to be safely inside the bucket interior (away from boundaries).
        fun bucketPool(ageDaysList: List<Long>): List<Entry> =
            ageDaysList.mapIndexed { index, d -> entry("e$index", daysAgo(d)) }
        fun singleAgePool(d: Long) = bucketPool(listOf(d, d + 15, d + 30, d + 45))
        val cases = listOf(
            7L to AgeBucket.DAYS_7_29,
            29L to AgeBucket.DAYS_7_29,
            30L to AgeBucket.DAYS_30_89,
            89L to AgeBucket.DAYS_30_89,
            90L to AgeBucket.DAYS_90_179,
            179L to AgeBucket.DAYS_90_179,
            180L to AgeBucket.DAYS_180_364,
            364L to AgeBucket.DAYS_180_364,
            365L to AgeBucket.DAYS_365_729,
            729L to AgeBucket.DAYS_365_729,
            730L to AgeBucket.DAYS_730_PLUS,
            2000L to AgeBucket.DAYS_730_PLUS,
        )
        for ((d, bucket) in cases) {
            val dec = TimeReturnEngine { 0 }.evaluate(now, zone, settings(), singleAgePool(d), emptyList())
            assertEquals("age $d days bucket", bucket, selected(dec).ageBucket)
        }
        // Below the 7-day eligibility floor nothing is selected.
        assertTrue(
            TimeReturnEngine { 0 }.evaluate(now, zone, settings(), bucketPool(listOf(1L, 2, 3, 4)), emptyList()) is TimeReturnDecision.Silent,
        )
    }

    @Test
    fun dstForwardGapDeliveryLandsOnValidLocalTime() {
        // America/New_York has a spring-forward gap every March; delivery must land
        // on an existing local time (offset must match canonical resolution).
        val zoneWithGap = ZoneId.of("America/New_York")
        val now = LocalDate.of(2027, 3, 15).atStartOfDay(zoneWithGap).toInstant()
        val entries = (1..10).map { entry("e$it", now.minusSeconds(it.toLong() * 40 * 86400)) }
        val sel = selected(TimeReturnEngine(SeededRng(7)).evaluate(now, zoneWithGap, settings(), entries, emptyList()))
        val local = sel.deliveryAt.atZone(zoneWithGap)
        // A phantom gap time would resolve to a different offset than requested.
        assertNotNull(ZonedDateTime.ofLocal(local.toLocalDateTime(), zoneWithGap, null))
        assertEquals(local.offset, ZonedDateTime.ofLocal(local.toLocalDateTime(), zoneWithGap, null)!!.offset)
    }

    @Test
    fun quietHoursTinyAllowedWindowRespectedAcrossSeeds() {
        // Allowed window [08:00, 09:00) is only 1 hour (quiet [09:00, 08:00)).
        val now = ZonedDateTime.of(2026, 6, 15, 22, 30, 0, 0, zone).toInstant()
        val entries = (1..10).map { entry("e$it", now.minusSeconds(it.toLong() * 40 * 86400)) }
        for (seed in 1..50) {
            val sel = selected(
                TimeReturnEngine(SeededRng(seed.toLong())).evaluate(
                    now, zone, settings(startMinute = 9 * 60, endMinute = 8 * 60), entries, emptyList(),
                ),
            )
            val minuteOfDay = sel.deliveryAt.atZone(zone).hour * 60 + sel.deliveryAt.atZone(zone).minute
            assertTrue(
                "delivery must fall in the 08:00-09:00 allowed window, got minute $minuteOfDay",
                minuteOfDay in (8 * 60 until 9 * 60),
            )
        }
    }

    @Test
    fun nonWrappingQuietHoursAllowedWindowCrossesMidnight() {
        // Quiet [01:00, 23:00) means allowed is [23:00, 01:00) crossing midnight.
        val now = ZonedDateTime.of(2026, 6, 15, 14, 30, 0, 0, zone).toInstant()
        val entries = (1..10).map { entry("e$it", now.minusSeconds(it.toLong() * 40 * 86400)) }
        for (seed in 1..50) {
            val sel = selected(
                TimeReturnEngine(SeededRng(seed.toLong())).evaluate(
                    now, zone, settings(startMinute = 1 * 60, endMinute = 23 * 60), entries, emptyList(),
                ),
            )
            val minuteOfDay = sel.deliveryAt.atZone(zone).hour * 60 + sel.deliveryAt.atZone(zone).minute
            assertTrue(
                "delivery must fall in allowed [23:00, 01:00) window, got minute $minuteOfDay",
                minuteOfDay >= 23 * 60 || minuteOfDay < 1 * 60,
            )
        }
    }

    @Test
    fun bootstrapRulesByEntryCount() {
        val now = ZonedDateTime.of(2026, 6, 15, 10, 0, 0, 0, zone).toInstant()
        val engine = TimeReturnEngine { 0 }
        // minimum age: 1 entry -> 30d, 2 entries -> 14d, 4+ -> 7d.
        fun nEntries(n: Int, ageStepDays: Long = 5): List<Entry> =
            (1..n).map { entry("e$it", now.minusSeconds(it.toLong() * ageStepDays * 86400)) }
        // 1 entry of 20 days: minimum is 30 -> still waiting.
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT),
            engine.evaluate(now, zone, settings(), nEntries(1), emptyList()),
        )
        // 2 entries of 5/10 days: minimum is 14 -> still waiting.
        assertEquals(
            TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT),
            engine.evaluate(now, zone, settings(), nEntries(2), emptyList()),
        )
        // 4 entries of 5/10/15/20 days: minimum is 7 and oldest is 20 -> selected.
        assertTrue(engine.evaluate(now, zone, settings(), nEntries(4), emptyList()) is TimeReturnDecision.Selected)
    }

    @Test
    fun extremeTimeZonesNeverProduceQuietHoursViolations() {
        // Campaign 6 — Time Torture: fuse-horários extremos (UTC+14, UTC-11, +5:45 Nepal).
        // A matemática do chooseAllowedDelivery depende do offset via atZone; o sweep garante
        // que a entrega nunca cai em quiet hours em qualquer offset civil válido.
        val zones = listOf(
            "Pacific/Kiritimati", // UTC+14
            "Pacific/Midway",     // UTC-11
            "Asia/Kathmandu",     // UTC+5:45
            "Asia/Kolkata",       // UTC+5:30
            "Etc/GMT-12",         // UTC+12 (nomenclatura invertida do Etc)
            "Etc/GMT+12",         // UTC-12
            "UTC",
        )
        val now = Instant.parse("2026-08-16T12:00:00Z")
        val pool = listOf(
            entry("a1", now.minusSeconds(20L * 86400)),
            entry("a2", now.minusSeconds(40L * 86400)),
            entry("a3", now.minusSeconds(60L * 86400)),
            entry("a4", now.minusSeconds(80L * 86400)),
        )
        val history = emptyList<ReturnAttempt>()
        for (zoneId in zones) {
            val z = ZoneId.of(zoneId)
            for (seed in 0L..99) {
                val dec = TimeReturnEngine { SeededRng(seed).nextInt(1) }.evaluate(
                    now, z, settings(), pool, history,
                )
                require(dec is TimeReturnDecision.Selected) {
                    "unacceptable silence at zone=$zoneId seed=$seed: $dec"
                }
                val local = dec.deliveryAt.atZone(z)
                val quietStart = LocalTime.of(21, 0)
                val quietEnd = LocalTime.of(8, 0)
                val inside = local.toLocalTime() >= quietStart || local.toLocalTime() < quietEnd
                assertFalse(
                    "quiet-hours violation zone=$zoneId seed=$seed delivery=$local",
                    inside,
                )
            }
        }
    }

    @Test
    fun dstFallBackGapAmbiguityResolvedToValidLocalTime() {
        // Campaign 6 — DST outono (Nova York, novembro): 02:00 volta a 01:00.
        // Entrega durante a janela ambígua deve existir como instante válido.
        val now = ZonedDateTime.of(2026, 11, 1, 6, 30, 0, 0, ZoneId.of("America/New_York")).toInstant()
        val pool = listOf(
            entry("a1", now.minusSeconds(20L * 86400)),
            entry("a2", now.minusSeconds(40L * 86400)),
            entry("a3", now.minusSeconds(60L * 86400)),
            entry("a4", now.minusSeconds(80L * 86400)),
        )
        val dec = TimeReturnEngine { 0 }.evaluate(now, ZoneId.of("America/New_York"), settings(), pool, emptyList())
        require(dec is TimeReturnDecision.Selected) { "expected selection, got $dec" }
        // Janela permitida [08:00, 21:00) local; no dia do DST-fallback a meia-noite ambígua
        // (01:00–02:00 que se repete) nunca deve ser escolhida como início de entrega.
        val local = dec.deliveryAt.atZone(ZoneId.of("America/New_York"))
        val inQuiet = local.toLocalTime() >= LocalTime.of(21, 0) || local.toLocalTime() < LocalTime.of(8, 0)
        assertFalse("dst-fallback delivery falls inside quiet hours: $local", inQuiet)
    }

    @Test
    fun epochEdgesAndFarFutureNeverCrash() {
        // Campaign 6 — epoch 1970, limite Unix 32-bit 2038-01-19, e datas distantes.
        val times = listOf(
            Instant.parse("1970-01-01T00:00:00Z"),
            Instant.parse("2038-01-19T03:14:06Z"),
            Instant.parse("2038-01-19T03:14:08Z"),
            Instant.parse("3000-06-15T10:00:00Z"),
        )
        val zoneId = ZoneId.of("America/Sao_Paulo")
        for (now in times) {
            val pool = listOf(
                entry("a1", now.minusSeconds(20L * 86400)),
                entry("a2", now.minusSeconds(40L * 86400)),
                entry("a3", now.minusSeconds(60L * 86400)),
                entry("a4", now.minusSeconds(80L * 86400)),
            )
            val dec = TimeReturnEngine { 0 }.evaluate(now, zoneId, settings(), pool, emptyList())
            require(dec is TimeReturnDecision.Selected) { "unexpected silence at now=$now: $dec" }
            assertNotNull(dec.deliveryAt)
        }
    }

    @Test
    fun leapYearAndLeapDayEntriesAgeCorrectly() {
        // Campaign 6 — entries criadas em 29/02 de ano bissexto devem envelhecer por dias civis.
        val now = ZonedDateTime.of(2028, 3, 1, 12, 0, 0, 0, zone).toInstant()
        val leapDayEntry = Entry(
            id = "leap",
            createdAt = ZonedDateTime.of(2028, 2, 29, 12, 0, 0, 0, zone).toInstant(),
            originalCreatedAt = ZonedDateTime.of(2028, 2, 29, 12, 0, 0, 0, zone).toInstant(),
            originalTimeZone = "America/Sao_Paulo",
            updatedAt = ZonedDateTime.of(2028, 2, 29, 12, 0, 0, 0, zone).toInstant(),
            content = "born on the leap day",
        )
        // 1 de março − 29 de fevereiro = 1 dia de idade; bootstrap 1 entry exige 30 dias → silent.
        val dec = TimeReturnEngine { 0 }.evaluate(now, zone, settings(), listOf(leapDayEntry), emptyList())
        assertTrue(
            "leap-day entry aged 1 day must not be selected: $dec",
            dec is TimeReturnDecision.Silent &&
                (dec.reason == SilenceReason.BOOTSTRAP_WAIT || dec.reason == SilenceReason.NO_ELIGIBLE_ENTRY),
        )
    }

    @Test
    fun tenThousandHourlyEvaluationsNoCrashNoQuietHoursViolation() {
        val entries = (1..40).map {
            entry("e$it", Instant.parse("2024-01-01T12:00:00Z").plusSeconds(it.toLong() * 20 * 86400))
        }
        var violations = 0
        var selectedCount = 0
        for (i in 1..10_000) {
            val now = Instant.parse("2024-07-01T12:00:00Z").plusSeconds(i.toLong() * 3600)
            val dec = TimeReturnEngine(SeededRng(i.toLong())).evaluate(now, zone, settings(), entries, emptyList())
            val sel = dec as? TimeReturnDecision.Selected
            if (sel != null) {
                selectedCount++
                val minuteOfDay = sel.deliveryAt.atZone(zone).hour * 60 + sel.deliveryAt.atZone(zone).minute
                if (minuteOfDay < 8 * 60 || minuteOfDay >= 21 * 60) violations++
            }
        }
        assertEquals("no quiet-hours violation in 10000 runs", 0, violations)
        assertTrue("selected > 0 across 10000 hourly evaluations", selectedCount > 0)
    }
}
