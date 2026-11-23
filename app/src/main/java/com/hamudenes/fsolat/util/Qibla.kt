package com.hamudenes.fsolat.util

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Qibla direction (bearing to the Kaaba in Mecca) plus optional device heading helpers.
 */
object Qibla {

    const val KAABA_LAT = 21.4224779
    const val KAABA_LON = 39.8251832

    private const val DEG = Math.PI / 180.0
    private const val RAD = 180.0 / Math.PI

    /** Great-circle initial bearing from the given coordinates to the Kaaba, in degrees [0, 360). */
    fun bearing(latitude: Double, longitude: Double): Double {
        val phi1 = latitude * DEG
        val phi2 = KAABA_LAT * DEG
        val deltaLambda = (KAABA_LON - longitude) * DEG
        val y = sin(deltaLambda) * cos(phi2)
        val x = cos(phi1) * sin(phi2) - sin(phi1) * cos(phi2) * cos(deltaLambda)
        val theta = atan2(y, x) * RAD
        return (theta + 360.0) % 360.0
    }

    /** Great-circle distance in kilometres to the Kaaba. */
    fun distanceKm(latitude: Double, longitude: Double): Double {
        val phi1 = latitude * DEG
        val phi2 = KAABA_LAT * DEG
        val dPhi = (KAABA_LAT - latitude) * DEG
        val dLambda = (KAABA_LON - longitude) * DEG
        val a = sin(dPhi / 2) * sin(dPhi / 2) +
            cos(phi1) * cos(phi2) * sin(dLambda / 2) * sin(dLambda / 2)
        val c = 2 * acos(a.coerceIn(0.0, 1.0))
        return 6371.0 * c
    }

    /** Shortest signed rotation from [current] to [target] in degrees, in (-180, 180]. */
    fun delta(current: Double, target: Double): Double {
        var d = (target - current) % 360.0
        if (d > 180.0) d -= 360.0
        if (d < -180.0) d += 360.0
        return d
    }

    fun compassPoint(bearing: Double): String {
        val dirs = listOf("K", "KD", "D", "GD", "G", "GB", "B", "KB")
        val idx = (floor((bearing + 22.5) / 45.0).toInt()) % 8
        return dirs[abs(idx)]
    }
}

/** Formats coordinates as "41.01° K, 28.98° D" style strings. */
object CoordFormat {
    fun format(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "K" else "G"
        val lonDir = if (lon >= 0) "D" else "B"
        return java.util.Locale.US.let {
            String.format(it, "%.4f° %s, %.4f° %s", abs(lat), latDir, abs(lon), lonDir)
        }
    }

    fun short(lat: Double, lon: Double): String {
        val latDir = if (lat >= 0) "N" else "S"
        val lonDir = if (lon >= 0) "E" else "W"
        return String.format(java.util.Locale.US, "%.2f° %s, %.2f° %s", abs(lat), latDir, abs(lon), lonDir)
    }
}

/** Simple degrees/minutes formatter for the compass dial. */
fun Double.toDegreesLabel(): String = String.format(java.util.Locale.US, "%d°", this.toInt())
