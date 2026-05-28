package com.sleepenforcer.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

object AlarmUtils {

    private const val WARNING_REQUEST_CODE = 1001
    private const val SHUTDOWN_REQUEST_CODE = 1002

    const val ALARM_TYPE_EXTRA = "ALARM_TYPE"
    const val ALARM_TYPE_WARNING = "WARNING"
    const val ALARM_TYPE_SHUTDOWN = "SHUTDOWN"

    /**
     * Schedules separate alarms for warning notification and shutdown/lock screen dynamically
     * using SleepScheduleManager to guarantee exact timestamps and extension alignment.
     */
    fun scheduleSleepCheck(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val scheduleManager = SleepScheduleManager(context)

        // Android 12+ Exact alarm permission check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("AlarmUtils", "Exact alarm permission is not granted.")
        }

        val warningTimeMs = scheduleManager.getNextWarningTimeMs()
        val shutdownTimeMs = scheduleManager.getNextShutdownTimeMs()

        // Schedule warning alarm
        scheduleAlarmAtMs(context, alarmManager, warningTimeMs, WARNING_REQUEST_CODE, ALARM_TYPE_WARNING)

        // Schedule shutdown alarm
        scheduleAlarmAtMs(context, alarmManager, shutdownTimeMs, SHUTDOWN_REQUEST_CODE, ALARM_TYPE_SHUTDOWN)
    }

    private fun scheduleAlarmAtMs(
        context: Context,
        alarmManager: AlarmManager,
        triggerTimeMs: Long,
        requestCode: Int,
        alarmType: String
    ) {
        val intent = Intent(context, SleepReceiver::class.java).apply {
            putExtra(ALARM_TYPE_EXTRA, alarmType)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
            Log.d("AlarmUtils", "$alarmType alarm scheduled (EXACT) at timestamp: $triggerTimeMs")
        } catch (e: SecurityException) {
            Log.w("AlarmUtils", "Failed to set exact alarm, trying regular alarm.", e)
            try {
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
                Log.d("AlarmUtils", "$alarmType alarm scheduled (REGULAR) at timestamp: $triggerTimeMs")
            } catch (e2: Exception) {
                Log.e("AlarmUtils", "Failed to set alarm.", e2)
            }
        }
    }
}

