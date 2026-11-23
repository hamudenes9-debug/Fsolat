package com.hamudenes.fsolat.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hamudenes.fsolat.data.AppLanguage
import com.hamudenes.fsolat.data.AppSettings
import com.hamudenes.fsolat.data.DayLog
import com.hamudenes.fsolat.data.Locator
import com.hamudenes.fsolat.data.SettingsRepository
import com.hamudenes.fsolat.data.ThemeMode
import com.hamudenes.fsolat.data.TrackingRepository
import com.hamudenes.fsolat.R
import com.hamudenes.fsolat.notify.PrayerNotifications
import com.hamudenes.fsolat.prayer.CalcMethod
import com.hamudenes.fsolat.prayer.HijriDate
import com.hamudenes.fsolat.prayer.Madhab
import com.hamudenes.fsolat.prayer.Prayer
import com.hamudenes.fsolat.prayer.PrayerService
import com.hamudenes.fsolat.prayer.label
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Live clock; ticks once per second so countdowns and the daily view stay accurate. */
private fun clockFlow(): kotlinx.coroutines.flow.Flow<Long> = flow {
    while (true) {
        emit(System.currentTimeMillis())
        delay(1000)
    }
}

data class UiState(
    val now: Long = System.currentTimeMillis(),
    val today: LocalDate = LocalDate.now(),
    val current: PrayerService.PrayerTime? = null,
    val next: PrayerService.PrayerTime? = null,
    val progress: Float = 0f,
    val dayTimes: PrayerService.DayTimes? = null,
    val tomorrowTimes: PrayerService.DayTimes? = null,
    val logs: List<DayLog> = emptyList(),
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCompleted: Int = 0,
    val monthAverage: Float = 0f,
    val hijriLabel: String = ""
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val settingsRepo = SettingsRepository(app)
    private val trackingRepo = TrackingRepository(app)
    val zone: ZoneId = ZoneId.systemDefault()

    val settings: StateFlow<AppSettings> =
        settingsRepo.settings.stateIn(viewModelScope, SharingStarted.Eagerly, AppSettings())

    private val refreshTick = MutableStateFlow(0)

    val uiState: StateFlow<UiState> = combine(
        settings,
        trackingRepo.recentLogs(120),
        clockFlow(),
        refreshTick
    ) { cfg, logs, now, _ ->
        buildState(cfg, logs, now)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UiState())

    private fun buildState(cfg: AppSettings, logs: List<DayLog>, now: Long): UiState {
        val today = java.time.Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val serviceSettings = PrayerService.Settings(cfg.method, cfg.madhab, cfg.adjustments)
        val todayTimes = PrayerService.timesFor(
            today, cfg.location.latitude, cfg.location.longitude, zone, serviceSettings
        )
        val tomorrow = PrayerService.timesFor(
            today.plusDays(1), cfg.location.latitude, cfg.location.longitude, zone, serviceSettings
        )
        val (current, next) = PrayerService.nextPrayer(
            now, cfg.location.latitude, cfg.location.longitude, zone, serviceSettings
        )
        val progress = if (current != null && next != null && next.epochMillis > current.epochMillis) {
            ((now - current.epochMillis).toFloat() / (next.epochMillis - current.epochMillis)).coerceIn(0f, 1f)
        } else 0f

        val ordered = logs.sortedByDescending { it.date }
        val monthLogs = ordered.filter {
            it.date.year == today.year && it.date.month == today.month
        }
        val monthAverage = if (monthLogs.isEmpty()) 0f
        else monthLogs.sumOf { it.count }.toFloat() / (monthLogs.size * 5f)

        return UiState(
            now = now,
            today = today,
            current = current,
            next = next,
            progress = progress,
            dayTimes = todayTimes,
            tomorrowTimes = tomorrow,
            logs = ordered,
            currentStreak = com.hamudenes.fsolat.data.StreakCalculator.currentStreak(ordered),
            longestStreak = com.hamudenes.fsolat.data.StreakCalculator.longestStreak(ordered),
            totalCompleted = com.hamudenes.fsolat.data.StreakCalculator.totalCompleted(ordered),
            monthAverage = monthAverage,
            hijriLabel = HijriDate.format(today)
        )
    }

    fun togglePrayer(prayer: Prayer) {
        viewModelScope.launch {
            trackingRepo.toggle(LocalDate.now(zone), prayer.key)
        }
    }

    fun setDone(date: LocalDate, prayer: Prayer, done: Boolean) {
        viewModelScope.launch { trackingRepo.setDone(date, prayer.key, done) }
    }

    // --- Settings actions -------------------------------------------------

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settingsRepo.setTheme(mode) }

    fun setLanguage(language: AppLanguage) = viewModelScope.launch {
        settingsRepo.setLanguage(language)
        applyLocale(language)
    }

    fun setMethod(method: CalcMethod) = viewModelScope.launch { settingsRepo.setMethod(method) }

    fun setMadhab(madhab: Madhab) = viewModelScope.launch { settingsRepo.setMadhab(madhab) }

    fun setUse24Hour(enabled: Boolean) = viewModelScope.launch { settingsRepo.setUse24Hour(enabled) }

    fun setReminderBefore(minutes: Int) = viewModelScope.launch {
        settingsRepo.setReminderBefore(minutes)
    }

    fun setAdjustment(key: String, minutes: Int) = viewModelScope.launch {
        settingsRepo.setAdjustment(key, minutes)
    }

    fun resetAdjustments() = viewModelScope.launch { settingsRepo.resetAdjustments() }

    fun setCity(name: String, latitude: Double, longitude: Double) = viewModelScope.launch {
        settingsRepo.setLocation(name, latitude, longitude, useDevice = false)
    }

    fun setNotifications(enabled: Boolean) = viewModelScope.launch {
        settingsRepo.setNotifications(enabled)
        if (enabled) {
            PrayerNotifications.enable(getApplication())
            refreshReminders()
        } else {
            PrayerNotifications.disable(getApplication())
        }
    }

    /** Recomputes and stores the reminder list for the worker (3 days ahead). */
    fun refreshReminders() {
        val app = getApplication<Application>()
        val cfg = settings.value
        if (!cfg.notificationsEnabled) return
        val serviceSettings = PrayerService.Settings(cfg.method, cfg.madhab, cfg.adjustments)
        val reminders = mutableListOf<PrayerNotifications.Reminder>()
        val today = LocalDate.now(zone)
        for (i in 0..2) {
            val times = PrayerService.timesFor(
                today.plusDays(i.toLong()),
                cfg.location.latitude,
                cfg.location.longitude,
                zone,
                serviceSettings
            )
            for (pt in times.list.filter { it.prayer.isPrayer }) {
                val stamp = pt.epochMillis - cfg.reminderBeforeMinutes * 60_000L
                if (stamp > System.currentTimeMillis()) {
                    reminders += PrayerNotifications.Reminder(
                        atMillis = stamp,
                        title = app.getString(R.string.notif_title, pt.prayer.label(app)),
                        body = app.getString(
                            R.string.notif_body,
                            pt.prayer.label(app),
                            cfg.reminderBeforeMinutes
                        )
                    )
                }
            }
        }
        PrayerNotifications.saveReminders(app, reminders)
    }

    fun onLocationPermissionGranted() = viewModelScope.launch {
        val result = withContext(Dispatchers.IO) { Locator.lastKnown(getApplication()) }
        if (result != null) {
            settingsRepo.setLocation(
                "Konumum",
                result.latitude,
                result.longitude,
                useDevice = true
            )
            refreshReminders()
        }
    }

    private fun applyLocale(language: AppLanguage) {
        val app = getApplication<Application>()
        app.getSharedPreferences("fsolat_locale", Context.MODE_PRIVATE)
            .edit()
            .putString("lang", language.id)
            .apply()
    }

    fun formatTime(epochMillis: Long, use24h: Boolean): String =
        PrayerService.formatTime(epochMillis, zone, use24h)

    companion object {
        val dayFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMMM EEEE", Locale.getDefault())
        val shortFormatter: DateTimeFormatter =
            DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    }
}
