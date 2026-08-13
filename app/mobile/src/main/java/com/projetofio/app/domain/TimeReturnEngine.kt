package com.projetofio.app.domain

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

fun interface ReturnRandom {
    fun nextInt(bound: Int): Int
}

sealed interface TimeReturnDecision {
    data class Selected(
        val entryId: String,
        val ageBucket: AgeBucket,
        val deliveryAt: Instant,
        val windowEnd: Instant,
    ) : TimeReturnDecision

    data class Silent(val reason: SilenceReason) : TimeReturnDecision
}

enum class SilenceReason {
    CONSENT_DISABLED,
    PENDING_RETURN,
    FREQUENCY_CAP,
    BOOTSTRAP_WAIT,
    NO_ELIGIBLE_ENTRY,
}

class TimeReturnEngine(
    private val random: ReturnRandom,
) {
    fun nextBootstrapOpportunity(
        now: Instant,
        zone: ZoneId,
        entries: List<Entry>,
        settings: AppSettings,
    ): Instant? {
        val active = entries.filter { it.deletedAt == null && it.returnMode == ReturnMode.ELIGIBLE }
        val oldest = active.minByOrNull { it.originalCreatedAt } ?: return null
        val (startDay, endDay) = when {
            active.size >= 4 -> 7L to 14L
            active.size >= 2 -> 14L to 21L
            else -> 30L to 45L
        }
        val dayOffset = startDay + safeIndex((endDay - startDay + 1).toInt())
        val candidateDate = oldest.originalCreatedAt.atZone(zone).toLocalDate().plusDays(dayOffset)
        val atAllowedStart = atLocal(zone, candidateDate, localTime(settings.quietHoursEndMinute)).toInstant()
        return if (atAllowedStart > now) atAllowedStart else now
    }

    fun evaluate(
        now: Instant,
        zone: ZoneId,
        settings: AppSettings,
        entries: List<Entry>,
        history: List<ReturnAttempt>,
    ): TimeReturnDecision {
        if (settings.returnConsentState != ReturnConsentState.ENABLED) {
            return TimeReturnDecision.Silent(SilenceReason.CONSENT_DISABLED)
        }
        if (history.any { it.state in PENDING_STATES }) {
            return TimeReturnDecision.Silent(SilenceReason.PENDING_RETURN)
        }
        val capBoundary = now.minus(7, ChronoUnit.DAYS)
        if (history.any { it.createdAt > capBoundary && it.state != ReturnState.CANCELLED }) {
            return TimeReturnDecision.Silent(SilenceReason.FREQUENCY_CAP)
        }

        val active = entries.filter { it.deletedAt == null && it.returnMode == ReturnMode.ELIGIBLE }
        val minimumAgeDays = bootstrapMinimumAgeDays(active.size)
        val oldestAge = active.maxOfOrNull { ageDays(it, now, zone) } ?: -1
        if (oldestAge < minimumAgeDays) {
            return TimeReturnDecision.Silent(SilenceReason.BOOTSTRAP_WAIT)
        }

        val previouslySelected = history.mapTo(mutableSetOf()) { it.entryId }
        val eligible = active.filter {
            val age = ageDays(it, now, zone)
            age >= 7 && it.id !in previouslySelected
        }
        if (eligible.isEmpty()) {
            return TimeReturnDecision.Silent(SilenceReason.NO_ELIGIBLE_ENTRY)
        }

        val grouped = eligible.groupBy { ageBucket(ageDays(it, now, zone)) }
        val buckets = grouped.keys.sortedBy { it.ordinal }
        val bucket = buckets[safeIndex(buckets.size)]
        val candidates = checkNotNull(grouped[bucket]).sortedBy { it.id }
        val entry = candidates[safeIndex(candidates.size)]
        val deliveryAt = chooseAllowedDelivery(now, zone, settings)
        return TimeReturnDecision.Selected(
            entryId = entry.id,
            ageBucket = bucket,
            deliveryAt = deliveryAt,
            windowEnd = deliveryAt.plus(24, ChronoUnit.HOURS),
        )
    }

    private fun chooseAllowedDelivery(now: Instant, zone: ZoneId, settings: AppSettings): Instant {
        val localNow = now.atZone(zone)
        val startMinute = settings.quietHoursEndMinute
        val endMinute = settings.quietHoursStartMinute
        val start = localTime(startMinute)
        val end = localTime(endMinute)

        val allowedStart: ZonedDateTime
        val allowedEnd: ZonedDateTime
        if (startMinute < endMinute) {
            when {
                localNow.toLocalTime() < start -> {
                    allowedStart = atLocal(zone, localNow.toLocalDate(), start)
                    allowedEnd = atLocal(zone, localNow.toLocalDate(), end)
                }
                localNow.toLocalTime() < end -> {
                    allowedStart = localNow
                    allowedEnd = atLocal(zone, localNow.toLocalDate(), end)
                }
                else -> {
                    val tomorrow = localNow.toLocalDate().plusDays(1)
                    allowedStart = atLocal(zone, tomorrow, start)
                    allowedEnd = atLocal(zone, tomorrow, end)
                }
            }
        } else {
            // Non-wrapping quiet hours imply an allowed window that crosses midnight.
            if (localNow.toLocalTime() >= start || localNow.toLocalTime() < end) {
                allowedStart = localNow
                val endDate = if (localNow.toLocalTime() < end) localNow.toLocalDate() else localNow.toLocalDate().plusDays(1)
                allowedEnd = atLocal(zone, endDate, end)
            } else {
                allowedStart = atLocal(zone, localNow.toLocalDate(), start)
                allowedEnd = atLocal(zone, localNow.toLocalDate().plusDays(1), end)
            }
        }
        val seconds = Duration.between(allowedStart.toInstant(), allowedEnd.toInstant()).seconds.coerceAtLeast(1)
        val offset = if (seconds > Int.MAX_VALUE) random.nextInt(Int.MAX_VALUE).toLong() else safeIndex(seconds.toInt()).toLong()
        return allowedStart.toInstant().plusSeconds(offset)
    }

    private fun safeIndex(bound: Int): Int {
        require(bound > 0)
        return random.nextInt(bound).let { Math.floorMod(it, bound) }
    }

    private fun bootstrapMinimumAgeDays(entryCount: Int): Long = when {
        entryCount >= 4 -> 7
        entryCount >= 2 -> 14
        entryCount == 1 -> 30
        else -> Long.MAX_VALUE
    }

    private fun ageDays(entry: Entry, now: Instant, zone: ZoneId): Long =
        ChronoUnit.DAYS.between(entry.originalCreatedAt.atZone(zone).toLocalDate(), now.atZone(zone).toLocalDate())

    private fun ageBucket(days: Long): AgeBucket = when (days) {
        in 7..29 -> AgeBucket.DAYS_7_29
        in 30..89 -> AgeBucket.DAYS_30_89
        in 90..179 -> AgeBucket.DAYS_90_179
        in 180..364 -> AgeBucket.DAYS_180_364
        in 365..729 -> AgeBucket.DAYS_365_729
        else -> AgeBucket.DAYS_730_PLUS
    }

    private fun localTime(minuteOfDay: Int): LocalTime =
        LocalTime.of(minuteOfDay / 60, minuteOfDay % 60)

    private fun atLocal(zone: ZoneId, date: LocalDate, time: LocalTime): ZonedDateTime =
        ZonedDateTime.of(date, time, zone)

    companion object {
        private val PENDING_STATES = setOf(ReturnState.SELECTED, ReturnState.SCHEDULED, ReturnState.NOTIFIED)
    }
}
