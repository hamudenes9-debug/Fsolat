package com.hamudenes.fsolat.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

/**
 * Thin wrapper around the platform location manager. We deliberately avoid Play Services so
 * the app stays dependency-free and works on de-Googled devices.
 */
object Locator {

    data class Result(val latitude: Double, val longitude: Double)

    fun hasPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /**
     * Returns the best last-known fix (GPS first, then network, then passive).
     * Blocking call — run it off the main thread.
     */
    @SuppressLint("MissingPermission")
    fun lastKnown(context: Context): Result? {
        if (!hasPermission(context)) return null
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        ).filter { runCatching { manager.isProviderEnabled(it) }.getOrDefault(false) }

        var best: Location? = null
        for (provider in providers) {
            val location = runCatching { manager.getLastKnownLocation(provider) }.getOrNull() ?: continue
            if (best == null || location.time > best!!.time) best = location
        }
        return best?.let { Result(it.latitude, it.longitude) }
    }
}
