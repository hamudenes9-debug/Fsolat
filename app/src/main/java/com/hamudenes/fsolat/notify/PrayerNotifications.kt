package com.hamudenes.fsolat.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.hamudenes.fsolat.MainActivity
import com.hamudenes.fsolat.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Prayer reminders. A periodic worker wakes up a few times a day, looks at the upcoming
 * reminders snapshot and posts the notification for whichever prayer is due.
 */
object PrayerNotifications {

    const val CHANNEL_ID = "prayer_reminders"
    private const val WORK_NAME = "prayer_reminder_loop"
    private const val NOTIFICATION_ID = 4101
    private const val PREFS = "prayer_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java) ?: return
            if (manager.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    context.getString(R.string.channel_prayer_name),
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = context.getString(R.string.channel_prayer_desc)
                    enableVibration(true)
                }
                manager.createNotificationChannel(channel)
            }
        }
    }

    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** Schedules the repeating worker that delivers prayer reminders. */
    fun enable(context: Context) {
        ensureChannel(context)
        val request = PeriodicWorkRequestBuilder<PrayerReminderWorker>(15, TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(false).build())
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()
        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun disable(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        saveReminders(context, emptyList())
    }

    fun notifyNow(context: Context, title: String, body: String) {
        ensureChannel(context)
        if (!canPost(context)) return
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pending = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_adhan)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .build()
        runCatching {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        }
    }

    data class Reminder(val atMillis: Long, val title: String, val body: String)

    /** Persists the upcoming reminder list so the worker can read it without recomputing anything. */
    fun saveReminders(context: Context, reminders: List<Reminder>) {
        val array = JSONArray()
        reminders.forEach {
            array.put(
                JSONObject()
                    .put("at", it.atMillis)
                    .put("title", it.title)
                    .put("body", it.body)
            )
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("list", array.toString())
            .apply()
    }

    fun loadReminders(context: Context): List<Reminder> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString("list", null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                Reminder(o.getLong("at"), o.getString("title"), o.getString("body"))
            }
        }.getOrDefault(emptyList())
    }
}

/** Periodic worker: fires any reminder whose time has arrived since the last run. */
class PrayerReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    override fun doWork(): Result {
        val context = applicationContext
        val now = System.currentTimeMillis()
        // Fire reminders that became due in the last 20 minutes (worker runs every 15 min).
        val due = PrayerNotifications.loadReminders(context).filter { it.atMillis in (now - 20 * 60_000)..now }
        due.maxByOrNull { it.atMillis }?.let {
            PrayerNotifications.notifyNow(context, it.title, it.body)
        }
        return Result.success()
    }
}
