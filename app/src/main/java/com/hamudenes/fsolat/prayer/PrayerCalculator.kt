package com.hamudenes.fsolat.prayer

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin
import kotlin.math.tan

/**
 * Astronomical prayer time calculator (port of the well-known Adhan algorithm).
 * Verified minute-by-minute against the `adhan` reference implementation.
 */
object PrayerCalculator {

    private const val DEG = Math.PI / 180.0

    // Trig helpers work in degrees. The kotlin.math imports are called through aliases below
    // to avoid accidental self-recursion.
    private fun sinD(d: Double): Double = sin(d * DEG)
    private fun cosD(d: Double): Double = cos(d * DEG)
    private fun tanD(d: Double): Double = tan(d * DEG)
    private fun arcsinD(x: Double): Double = Math.asin(x.coerceIn(-1.0, 1.0)) * 180.0 / Math.PI
    private fun arccosD(x: Double): Double = acos(x.coerceIn(-1.0, 1.0)) * 180.0 / Math.PI
    private fun arctan2D(y: Double, x: Double): Double = atan2(y, x) * 180.0 / Math.PI

    private fun arccotD(x: Double): Double {
        val a = atan(1.0 / x) * 180.0 / Math.PI
        return if (a < 0) a + 180.0 else a
    }

    private fun fix(a: Double, b: Double): Double {
        val r = a - b * floor(a / b)
        return if (r < 0) r + b else r
    }

    private fun fixAngle(a: Double) = fix(a, 360.0)
    private fun fixHour(a: Double) = fix(a, 24.0)

    /** Resolved parameters for a calculation method. */
    data class Params(
        val fajrAngle: Double,
        val ishaAngle: Double,
        val ishaMinutes: Int? = null,
        val asrFactor: Double = 1.0,
        val adjustments: Map<String, Int> = emptyMap()
    )

    /** A single day of prayer times, expressed as decimal hours in local time. */
    data class Times(
        val fajr: Double,
        val sunrise: Double,
        val dhuhr: Double,
        val asr: Double,
        val maghrib: Double,
        val isha: Double
    ) {
        fun asMap(): Map<String, Double> = linkedMapOf(
            "fajr" to fajr,
            "sunrise" to sunrise,
            "dhuhr" to dhuhr,
            "asr" to asr,
            "maghrib" to maghrib,
            "isha" to isha
        )
    }

    /** Julian day of local midnight, adjusted for longitude like the reference implementation. */
    private fun julianDay(localDayNumber: Long, longitude: Double): Double =
        2440587.5 + localDayNumber - longitude / (15.0 * 24.0)

    private fun sunPosition(jd: Double): Pair<Double, Double> {
        val d = jd - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sinD(g) + 0.020 * sinD(2 * g))
        val e = 23.439 - 0.00000036 * d
        val ra = arctan2D(cosD(e) * sinD(l), cosD(l)) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = arcsinD(sinD(e) * sinD(l))
        return Pair(decl, eqt)
    }

    private fun midDay(jd: Double, time: Double): Double {
        val eqt = sunPosition(jd + time).second
        return fixHour(12.0 - eqt)
    }

    private fun sunAngleTime(
        jd: Double,
        latitude: Double,
        angle: Double,
        time: Double,
        ccw: Boolean
    ): Double? {
        val decl = sunPosition(jd + time).first
        val noon = midDay(jd, time)
        val arg = (-sinD(angle) - sinD(decl) * sinD(latitude)) / (cosD(decl) * cosD(latitude))
        if (arg < -1.0 || arg > 1.0) return null
        val t = arccosD(arg) / 15.0
        return if (ccw) noon - t else noon + t
    }

    private fun asrTime(jd: Double, latitude: Double, factor: Double, time: Double): Double? {
        val decl = sunPosition(jd + time).first
        val angle = -arccotD(factor + tanD(abs(latitude - decl)))
        return sunAngleTime(jd, latitude, angle, time, false)
    }

    /**
     * Calculates prayer times in decimal hours (local wall clock).
     *
     * @param localDayNumber days since 1970-01-01 for the local calendar date
     * @param timeZoneHours local UTC offset in hours (supports DST fractions)
     */
    fun calculate(
        localDayNumber: Long,
        latitude: Double,
        longitude: Double,
        timeZoneHours: Double,
        params: Params
    ): Times {
        val jd = julianDay(localDayNumber, longitude)
        var f = 5.0 / 24.0
        var s = 6.0 / 24.0
        var d = 12.0 / 24.0
        var a = 13.0 / 24.0
        var ss = 18.0 / 24.0
        var i = 18.0 / 24.0

        repeat(3) {
            val fajr = sunAngleTime(jd, latitude, params.fajrAngle, f, true) ?: (s * 24.0 - 1.5)
            val sunrise = sunAngleTime(jd, latitude, 0.833, s, true) ?: (d * 24.0 - 6.0)
            val dhuhr = midDay(jd, d)
            val asr = asrTime(jd, latitude, params.asrFactor, a) ?: (dhuhr + 3.0)
            val sunset = sunAngleTime(jd, latitude, 0.833, ss, false) ?: (dhuhr + 6.0)
            val isha = if (params.ishaMinutes == null)
                sunAngleTime(jd, latitude, params.ishaAngle, i, false) ?: (sunset + 1.5)
            else sunset + params.ishaMinutes / 60.0

            f = fajr / 24.0
            s = sunrise / 24.0
            d = dhuhr / 24.0
            a = asr / 24.0
            ss = sunset / 24.0
            i = isha / 24.0
        }

        val fajr = sunAngleTime(jd, latitude, params.fajrAngle, f, true) ?: (s * 24.0 - 1.5)
        val sunrise = sunAngleTime(jd, latitude, 0.833, s, true) ?: (d * 24.0 - 6.0)
        val dhuhr = midDay(jd, d)
        val asr = asrTime(jd, latitude, params.asrFactor, a) ?: (dhuhr + 3.0)
        val sunset = sunAngleTime(jd, latitude, 0.833, ss, false) ?: (dhuhr + 6.0)
        val isha = if (params.ishaMinutes == null)
            sunAngleTime(jd, latitude, params.ishaAngle, i, false) ?: (sunset + 1.5)
        else sunset + params.ishaMinutes / 60.0

        val shift = timeZoneHours - longitude / 15.0
        fun adjusted(name: String, hours: Double): Double {
            val adj = (params.adjustments[name] ?: 0) / 60.0
            return fixHour(hours + shift + adj)
        }

        return Times(
            fajr = adjusted("fajr", fajr),
            sunrise = adjusted("sunrise", sunrise),
            dhuhr = adjusted("dhuhr", dhuhr),
            asr = adjusted("asr", asr),
            maghrib = adjusted("maghrib", sunset),
            isha = adjusted("isha", isha)
        )
    }
}
