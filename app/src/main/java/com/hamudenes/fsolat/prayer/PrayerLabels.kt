package com.hamudenes.fsolat.prayer

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.hamudenes.fsolat.R

/** Localized display name of a prayer (or sunrise). */
@Composable
fun Prayer.label(): String = stringResource(
    when (this) {
        Prayer.FAJR -> R.string.prayer_fajr
        Prayer.SUNRISE -> R.string.prayer_sunrise
        Prayer.DHUHR -> R.string.prayer_dhuhr
        Prayer.ASR -> R.string.prayer_asr
        Prayer.MAGHRIB -> R.string.prayer_maghrib
        Prayer.ISHA -> R.string.prayer_isha
    }
)

/** Localized display name usable outside composition (notifications, workers). */
fun Prayer.label(context: Context): String = context.getString(
    when (this) {
        Prayer.FAJR -> R.string.prayer_fajr
        Prayer.SUNRISE -> R.string.prayer_sunrise
        Prayer.DHUHR -> R.string.prayer_dhuhr
        Prayer.ASR -> R.string.prayer_asr
        Prayer.MAGHRIB -> R.string.prayer_maghrib
        Prayer.ISHA -> R.string.prayer_isha
    }
)

/** Arabic-style short name shown in the big hero card. */
fun Prayer.arabicName(): String = when (this) {
    Prayer.FAJR -> "الفجر"
    Prayer.SUNRISE -> "الشروق"
    Prayer.DHUHR -> "الظهر"
    Prayer.ASR -> "العصر"
    Prayer.MAGHRIB -> "المغرب"
    Prayer.ISHA -> "العشاء"
}

@Composable
fun CalcMethod.label(): String = stringResource(
    when (this) {
        CalcMethod.DIYANET -> R.string.method_diyanet
        CalcMethod.DIYANET_15 -> R.string.method_diyanet15
        CalcMethod.MWL -> R.string.method_mwl
        CalcMethod.ISNA -> R.string.method_isna
        CalcMethod.EGYPT -> R.string.method_egypt
        CalcMethod.KARACHI -> R.string.method_karachi
        CalcMethod.UMM_AL_QURA -> R.string.method_umm_al_qura
        CalcMethod.GULF -> R.string.method_gulf
        CalcMethod.KUWAIT -> R.string.method_kuwait
        CalcMethod.QATAR -> R.string.method_qatar
        CalcMethod.SINGAPORE -> R.string.method_singapore
        CalcMethod.FRANCE -> R.string.method_france
    }
)

@Composable
fun CalcMethod.description(): String = stringResource(
    when (this) {
        CalcMethod.DIYANET -> R.string.method_diyanet_desc
        CalcMethod.DIYANET_15 -> R.string.method_diyanet15_desc
        CalcMethod.MWL -> R.string.method_mwl_desc
        CalcMethod.ISNA -> R.string.method_isna_desc
        CalcMethod.EGYPT -> R.string.method_egypt_desc
        CalcMethod.KARACHI -> R.string.method_karachi_desc
        CalcMethod.UMM_AL_QURA -> R.string.method_umm_al_qura_desc
        CalcMethod.GULF -> R.string.method_gulf_desc
        CalcMethod.KUWAIT -> R.string.method_kuwait_desc
        CalcMethod.QATAR -> R.string.method_qatar_desc
        CalcMethod.SINGAPORE -> R.string.method_singapore_desc
        CalcMethod.FRANCE -> R.string.method_france_desc
    }
)

@Composable
fun Madhab.label(): String = stringResource(
    if (this == Madhab.HANAFI) R.string.madhab_hanafi else R.string.madhab_shafi
)
