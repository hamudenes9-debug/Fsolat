package com.hamudenes.fsolat.prayer

/**
 * Calculation methods supported by the app. Angles and tune values mirror the
 * `adhan` reference library so results line up with what people expect.
 */
enum class CalcMethod(
    val id: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaMinutes: Int? = null,
    val adjustments: Map<String, Int> = emptyMap()
) {
    DIYANET(
        id = "diyanet",
        fajrAngle = 18.0,
        ishaAngle = 17.0,
        adjustments = mapOf("sunrise" to -7, "dhuhr" to 5, "asr" to 4, "maghrib" to 7)
    ),
    DIYANET_15(
        id = "diyanet15",
        fajrAngle = 18.0,
        ishaAngle = 15.0,
        adjustments = mapOf("sunrise" to -7, "dhuhr" to 5, "asr" to 4, "maghrib" to 7)
    ),
    MWL(
        id = "mwl",
        fajrAngle = 18.0,
        ishaAngle = 17.0
    ),
    ISNA(
        id = "isna",
        fajrAngle = 15.0,
        ishaAngle = 15.0
    ),
    EGYPT(
        id = "egypt",
        fajrAngle = 19.5,
        ishaAngle = 17.5
    ),
    KARACHI(
        id = "karachi",
        fajrAngle = 18.0,
        ishaAngle = 18.0
    ),
    UMM_AL_QURA(
        id = "umm_al_qura",
        fajrAngle = 18.5,
        ishaAngle = 0.0,
        ishaMinutes = 90
    ),
    GULF(
        id = "gulf",
        fajrAngle = 19.5,
        ishaAngle = 0.0,
        ishaMinutes = 90
    ),
    KUWAIT(
        id = "kuwait",
        fajrAngle = 18.0,
        ishaAngle = 17.5
    ),
    QATAR(
        id = "qatar",
        fajrAngle = 18.0,
        ishaAngle = 0.0,
        ishaMinutes = 90
    ),
    SINGAPORE(
        id = "singapore",
        fajrAngle = 20.0,
        ishaAngle = 18.0
    ),
    FRANCE(
        id = "france",
        fajrAngle = 12.0,
        ishaAngle = 12.0
    );

    fun toParams(asrFactor: Double): PrayerCalculator.Params = PrayerCalculator.Params(
        fajrAngle = fajrAngle,
        ishaAngle = ishaAngle,
        ishaMinutes = ishaMinutes,
        asrFactor = asrFactor,
        adjustments = adjustments
    )

    companion object {
        fun fromId(id: String?): CalcMethod = entries.firstOrNull { it.id == id } ?: DIYANET
    }
}

/** Asr shadow factor: Shafi/Hanbali/Maliki = 1, Hanafi = 2. */
enum class Madhab(val id: String, val factor: Double) {
    SHAFI("shafi", 1.0),
    HANAFI("hanafi", 2.0);

    companion object {
        fun fromId(id: String?): Madhab = entries.firstOrNull { it.id == id } ?: SHAFI
    }
}
