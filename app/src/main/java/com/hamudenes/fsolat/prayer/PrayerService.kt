package com.hamudenes.fsolat.prayer

import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

/** Names of the five daily prayers plus sunrise, used across the UI. */
enum class Prayer(val key: String) {
    FAJR("fajr"),
    SUNRISE("sunrise"),
    DHUHR("dhuhr"),
    ASR("asr"),
    MAGHRIB("maghrib"),
    ISHA("isha");

    val isPrayer: Boolean get() = this != SUNRISE

    companion object {
        val prayersOnly: List<Prayer> get() = listOf(FAJR, DHUHR, ASR, MAGHRIB, ISHA)
    }
}

/**
 * High level service that turns a date + location + settings into concrete [PrayerTime]s,
 * and answers "what is the current / next prayer" questions.
 */
object PrayerService {

    data class Settings(
        val method: CalcMethod = CalcMethod.DIYANET,
        val madhab: Madhab = Madhab.SHAFI,
        val adjustments: Map<String, Int> = emptyMap()
    )

    data class PrayerTime(val prayer: Prayer, val epochMillis: Long)

    data class DayTimes(
        val date: LocalDate,
        val list: List<PrayerTime>
    ) {
        fun timeOf(prayer: Prayer): PrayerTime? = list.firstOrNull { it.prayer == prayer }
    }

    private fun localDayNumber(date: LocalDate): Long = date.toEpochDay()

    private fun utcOffsetHours(date: LocalDate, zone: ZoneId): Double {
        val instant = date.atStartOfDay(zone).toInstant()
        val totalSeconds = zone.rules.getOffset(instant).totalSeconds
        return totalSeconds / 3600.0
    }

    /**
     * Computes prayer times for [date] at the given coordinates. The date is interpreted in [zone]
     * so that daylight saving transitions are handled by java.time.
     */
    fun timesFor(
        date: LocalDate,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        settings: Settings
    ): DayTimes {
        val params = settings.method.toParams(settings.madhab.factor).let { base ->
            base.copy(adjustments = combineAdjustments(base.adjustments, settings.adjustments))
        }
        val tz = utcOffsetHours(date, zone)
        val computed = PrayerCalculator.calculate(
            localDayNumber = localDayNumber(date),
            latitude = latitude,
            longitude = longitude,
            timeZoneHours = tz,
            params = params
        )
        val midnight = date.atStartOfDay(zone)
        val values = computed.asMap()
        val list = Prayer.entries.mapNotNull { prayer ->
            values[prayer.key]?.let { hours ->
                val minutesTotal = (hours * 60.0).toLong()
                val t = midnight.plusMinutes(minutesTotal)
                PrayerTime(prayer, t.toInstant().toEpochMilli())
            }
        }
        return DayTimes(date, list)
    }

    private fun combineAdjustments(
        base: Map<String, Int>,
        user: Map<String, Int>
    ): Map<String, Int> {
        if (user.isEmpty()) return base
        val merged = base.toMutableMap()
        user.forEach { (key, value) ->
            merged[key] = (merged[key] ?: 0) + value
        }
        return merged
    }

    /** Current or next prayer for [now], checking today and then the next couple of days. */
    fun nextPrayer(
        now: Long,
        latitude: Double,
        longitude: Double,
        zone: ZoneId,
        settings: Settings
    ): Pair<PrayerTime?, PrayerTime?> {
        val today = java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        var previous: PrayerTime? = null
        for (i in 0..2) {
            val day = timesFor(today.plusDays(i.toLong()), latitude, longitude, zone, settings)
            for (pt in day.list.filter { it.prayer.isPrayer }) {
                if (pt.epochMillis > now) return Pair(previous, pt)
                previous = pt
            }
        }
        return Pair(previous, null)
    }

    /** Remaining seconds until a future epoch millis, never negative. */
    fun remainingSeconds(targetEpochMillis: Long, now: Long): Long =
        ((targetEpochMillis - now) / 1000L).coerceAtLeast(0L)

    fun formatDuration(totalSeconds: Long): String {
        val h = totalSeconds / 3600
        val m = (totalSeconds % 3600) / 60
        val s = totalSeconds % 60
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
    }

    fun formatTime(epochMillis: Long, zone: ZoneId, use24h: Boolean): String {
        val t = java.time.Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalTime()
        return if (use24h) {
            String.format(Locale.US, "%02d:%02d", t.hour, t.minute)
        } else {
            var hour12 = t.hour % 12
            if (hour12 == 0) hour12 = 12
            val suffix = if (t.hour < 12) "AM" else "PM"
            String.format(Locale.US, "%d:%02d %s", hour12, t.minute, suffix)
        }
    }
}
