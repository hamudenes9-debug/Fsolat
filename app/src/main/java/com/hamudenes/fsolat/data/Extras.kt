package com.hamudenes.fsolat.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.trackingStore by preferencesDataStore(name = "fsolat_tracking")

/** A single day's prayer log: which prayers the user marked as performed. */
data class DayLog(
    val date: LocalDate,
    val done: Set<String>
) {
    val count: Int get() = done.size
}

/**
 * Stores daily prayer tracking (the "namaz günlüğü") plus simple streak bookkeeping.
 */
class TrackingRepository(private val context: Context) {

    private object Keys {
        fun day(date: LocalDate) = stringSetPreferencesKey("day_" + date.toString())
        val onboarded = booleanPreferencesKey("onboarded")
    }

    fun logFor(date: LocalDate): Flow<DayLog> = context.trackingStore.data.map { prefs ->
        DayLog(date, prefs[Keys.day(date)] ?: emptySet())
    }

    fun recentLogs(days: Int): Flow<List<DayLog>> = context.trackingStore.data.map { prefs ->
        val today = LocalDate.now()
        (0 until days).map { offset ->
            val date = today.minusDays(offset.toLong())
            DayLog(date, prefs[Keys.day(date)] ?: emptySet())
        }
    }

    val onboarded: Flow<Boolean> = context.trackingStore.data.map { it[Keys.onboarded] ?: false }

    suspend fun toggle(date: LocalDate, prayerKey: String) {
        context.trackingStore.edit { prefs ->
            val key = Keys.day(date)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (prayerKey in current) current - prayerKey else current + prayerKey
        }
    }

    suspend fun setDone(date: LocalDate, prayerKey: String, done: Boolean) {
        context.trackingStore.edit { prefs ->
            val key = Keys.day(date)
            val current = prefs[key] ?: emptySet()
            prefs[key] = if (done) current + prayerKey else current - prayerKey
        }
    }

    suspend fun markOnboarded() {
        context.trackingStore.edit { it[Keys.onboarded] = true }
    }
}

/** Streak helpers computed from a list of day logs. */
object StreakCalculator {

    /** Consecutive days (ending today or yesterday) where at least [required] prayers are done. */
    fun currentStreak(logs: List<DayLog>, required: Int = 1): Int {
        if (logs.isEmpty()) return 0
        var streak = 0
        var index = 0
        // Allow today to be still in progress: start from today, but if today is empty, skip it.
        if ((logs.firstOrNull()?.count ?: 0) < required) {
            index = 1
        }
        while (index < logs.size) {
            val log = logs[index]
            if (log.count >= required) {
                streak++
                index++
            } else {
                break
            }
        }
        return streak
    }

    fun longestStreak(logs: List<DayLog>, required: Int = 1): Int {
        var best = 0
        var run = 0
        logs.forEach { log ->
            if (log.count >= required) {
                run++
                if (run > best) best = run
            } else {
                run = 0
            }
        }
        return best
    }

    fun totalCompleted(logs: List<DayLog>): Int = logs.sumOf { it.count }
}
