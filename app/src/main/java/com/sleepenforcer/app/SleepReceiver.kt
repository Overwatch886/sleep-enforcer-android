package com.sleepenforcer.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat

class SleepReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "SleepEnforcerChannel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("SleepReceiver", "onReceive called")
        val alarmType = intent?.getStringExtra(AlarmUtils.ALARM_TYPE_EXTRA)
        Log.d("SleepReceiver", "Alarm type: $alarmType")

        val scheduleManager = SleepScheduleManager(context)

        when (alarmType) {
            AlarmUtils.ALARM_TYPE_WARNING -> {
                // For warning, we shouldn't check isInsideSleepWindow() because by definition the warning time
                // occurs 5 minutes before the actual bedtime (which is the start of the sleep window).
                // Instead, we verify that the warning alarm is firing close to its scheduled time to avoid stale alarms.
                val currentTimeMs = System.currentTimeMillis()
                val nextWarningTimeMs = scheduleManager.getNextWarningTimeMs(currentTimeMs - 60_000)
                
                val diff = Math.abs(currentTimeMs - nextWarningTimeMs)
                if (diff < 180_000L) { // 3 minutes window
                    Log.d("SleepReceiver", "Sending warning notification (diff is $diff ms)")
                    sendWarningNotification(context)
                } else {
                    Log.d("SleepReceiver", "Ignoring warning alarm: too far from scheduled time (diff: $diff ms)")
                }
            }
            AlarmUtils.ALARM_TYPE_SHUTDOWN -> {
                // Only trigger lock screen if the current time is mathematically inside the sleep window!
                if (!scheduleManager.isInsideSleepWindow()) {
                    Log.d("SleepReceiver", "Ignoring shutdown alarm: current time is outside the sleep window (due to extension or daytime).")
                    return
                }
                Log.d("SleepReceiver", "Triggering lock screen")
                triggerLockScreen(context)
            }
            else -> {
                Log.w("SleepReceiver", "Unknown alarm type: $alarmType")
            }
        }
    }

    private fun sendWarningNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Enforcer Warnings",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warning notifications before the device locks."
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⏰ Sleep Warning")
            .setContentText("The device will lock in 5 minutes. Time to wrap up!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
        Log.d("SleepReceiver", "Warning notification sent")
    }

    private fun triggerLockScreen(context: Context) {
        // Clear any warning notifications
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)

        val lockIntent = Intent(context, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        context.startActivity(lockIntent)
        Log.d("SleepReceiver", "LockScreenActivity launched successfully!")
    }
}
