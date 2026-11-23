package com.hamudenes.fsolat.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.hamudenes.fsolat.prayer.CalcMethod
import com.hamudenes.fsolat.prayer.Madhab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsStore by preferencesDataStore(name = "fsolat_settings")

enum class ThemeMode(val id: String) {
    SYSTEM("system"),
    DARK("dark"),
    LIGHT("light");

    companion object {
        fun fromId(id: String?): ThemeMode = entries.firstOrNull { it.id == id } ?: DARK
    }
}

enum class AppLanguage(val id: String) {
    SYSTEM("system"),
    TURKISH("tr"),
    ENGLISH("en");

    companion object {
        fun fromId(id: String?): AppLanguage = entries.firstOrNull { it.id == id } ?: SYSTEM
    }
}

data class LocationSettings(
    val cityName: String = "İstanbul",
    val latitude: Double = 41.0082,
    val longitude: Double = 28.9784,
    val useDeviceLocation: Boolean = false
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: AppLanguage = AppLanguage.SYSTEM,
    val method: CalcMethod = CalcMethod.DIYANET,
    val madhab: Madhab = Madhab.SHAFI,
    val notificationsEnabled: Boolean = false,
    val reminderBeforeMinutes: Int = 15,
    val use24Hour: Boolean = true,
    val location: LocationSettings = LocationSettings(),
    val adjustments: Map<String, Int> = emptyMap()
)

/**
 * Persists every user preference in a single DataStore flow so the UI has one source of truth.
 */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val theme = stringPreferencesKey("theme_mode")
        val language = stringPreferencesKey("language")
        val method = stringPreferencesKey("calc_method")
        val madhab = stringPreferencesKey("madhab")
        val notifications = booleanPreferencesKey("notifications_enabled")
        val reminderBefore = stringPreferencesKey("reminder_before")
        val use24h = booleanPreferencesKey("use_24h")
        val cityName = stringPreferencesKey("city_name")
        val latitude = floatPreferencesKey("latitude")
        val longitude = floatPreferencesKey("longitude")
        val useDeviceLocation = booleanPreferencesKey("use_device_location")
        val adjustmentPrefix = "adjustment_"
    }

    val settings: Flow<AppSettings> = context.settingsStore.data.map { prefs ->
        val adjustments = listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha")
            .mapNotNull { key ->
                prefs.get(stringPreferencesKey(Keys.adjustmentPrefix + key))?.toIntOrNull()?.let { key to it }
            }
            .toMap()
        AppSettings(
            themeMode = ThemeMode.fromId(prefs[Keys.theme]),
            language = AppLanguage.fromId(prefs[Keys.language]),
            method = CalcMethod.fromId(prefs[Keys.method]),
            madhab = Madhab.fromId(prefs[Keys.madhab]),
            notificationsEnabled = prefs[Keys.notifications] ?: false,
            reminderBeforeMinutes = prefs[Keys.reminderBefore]?.toIntOrNull() ?: 15,
            use24Hour = prefs[Keys.use24h] ?: true,
            location = LocationSettings(
                cityName = prefs[Keys.cityName] ?: "İstanbul",
                latitude = prefs[Keys.latitude]?.toDouble() ?: 41.0082,
                longitude = prefs[Keys.longitude]?.toDouble() ?: 28.9784,
                useDeviceLocation = prefs[Keys.useDeviceLocation] ?: false
            ),
            adjustments = adjustments
        )
    }

    suspend fun setTheme(mode: ThemeMode) = context.settingsStore.edit { it[Keys.theme] = mode.id }

    suspend fun setLanguage(language: AppLanguage) =
        context.settingsStore.edit { it[Keys.language] = language.id }

    suspend fun setMethod(method: CalcMethod) =
        context.settingsStore.edit { it[Keys.method] = method.id }

    suspend fun setMadhab(madhab: Madhab) =
        context.settingsStore.edit { it[Keys.madhab] = madhab.id }

    suspend fun setNotifications(enabled: Boolean) =
        context.settingsStore.edit { it[Keys.notifications] = enabled }

    suspend fun setReminderBefore(minutes: Int) =
        context.settingsStore.edit { it[Keys.reminderBefore] = minutes.toString() }

    suspend fun setUse24Hour(enabled: Boolean) =
        context.settingsStore.edit { it[Keys.use24h] = enabled }

    suspend fun setLocation(cityName: String, latitude: Double, longitude: Double, useDevice: Boolean) {
        context.settingsStore.edit {
            it[Keys.cityName] = cityName
            it[Keys.latitude] = latitude.toFloat()
            it[Keys.longitude] = longitude.toFloat()
            it[Keys.useDeviceLocation] = useDevice
        }
    }

    suspend fun setAdjustment(key: String, minutes: Int) {
        context.settingsStore.edit { prefs ->
            prefs[stringPreferencesKey(Keys.adjustmentPrefix + key)] = minutes.toString()
        }
    }

    suspend fun resetAdjustments() {
        context.settingsStore.edit { prefs ->
            listOf("fajr", "sunrise", "dhuhr", "asr", "maghrib", "isha").forEach {
                prefs.remove(stringPreferencesKey(Keys.adjustmentPrefix + it))
            }
        }
    }
}
