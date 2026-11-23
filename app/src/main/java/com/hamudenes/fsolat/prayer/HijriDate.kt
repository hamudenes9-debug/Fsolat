package com.hamudenes.fsolat.prayer

import java.time.LocalDate
import java.util.Locale
import kotlin.math.floor

/**
 * Tabular (arithmetic) Hijri calendar conversion using the Kuwaiti algorithm. It can differ
 * by a day from local moon sighting, which is the expected behaviour for an app-level estimate.
 */
object HijriDate {

    private val monthNamesTr = arrayOf(
        "Muharrem", "Safer", "Rebiülevvel", "Rebiülahir", "Cemaziyelevvel", "Cemaziyelahir",
        "Recep", "Şaban", "Ramazan", "Şevval", "Zilkade", "Zilhicce"
    )

    private val monthNamesEn = arrayOf(
        "Muharram", "Safar", "Rabi al-Awwal", "Rabi al-Thani", "Jumada al-Awwal", "Jumada al-Thani",
        "Rajab", "Shaban", "Ramadan", "Shawwal", "Dhu al-Qadah", "Dhu al-Hijjah"
    )

    data class Hijri(val year: Int, val month: Int, val day: Int)

    /** Converts a Gregorian date to the tabular Islamic calendar. */
    fun convert(date: LocalDate): Hijri {
        var year = date.year
        var month = date.monthValue
        val day = date.dayOfMonth
        if (month < 3) {
            year -= 1
            month += 12
        }
        val a = floor(year / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()
        val jd = floor(365.25 * (year + 4716)).toInt() +
            floor(30.6001 * (month + 1)).toInt() + day + b - 1524

        val l0 = jd - 1948440 + 10632
        val n = floor((l0 - 1) / 10631.0).toInt()
        val l1 = l0 - 10631 * n + 354
        val j = (floor((10985 - l1) / 5316.0) * floor((50 * l1) / 17719.0) +
            floor(l1 / 5670.0) * floor((43 * l1) / 15238.0)).toInt()
        val l2 = l1 - (floor((30 - j) / 15.0) * floor((17719 * j) / 50.0)).toInt() -
            floor(j / 16.0).toInt() * floor((15238 * j) / 43.0).toInt() + 29
        val hMonth = floor((24 * l2) / 709.0).toInt()
        val hDay = l2 - floor((709 * hMonth) / 24.0).toInt()
        val hYear = 30 * n + j - 30

        return Hijri(hYear, hMonth.coerceIn(1, 12), hDay.coerceIn(1, 30))
    }

    fun format(date: LocalDate, turkish: Boolean = true): String {
        val h = convert(date)
        val names = if (turkish) monthNamesTr else monthNamesEn
        return String.format(Locale.US, "%d %s %d", h.day, names[h.month - 1], h.year)
    }

    fun isRamadan(date: LocalDate): Boolean = convert(date).month == 9
}
