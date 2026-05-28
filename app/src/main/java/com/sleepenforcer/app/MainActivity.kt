package com.sleepenforcer.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val PERMISSION_CODE = 100
    }
    
    private lateinit var statusLabel: TextView
    private lateinit var settingsButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    
    private var warningHour: Int = 21
    private var warningMinute: Int = 55
    private var shutdownHour: Int = 22
    private var shutdownMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val scheduleManager = SleepScheduleManager(this)
        if (scheduleManager.isInsideSleepWindow()) {
            Log.d("MainActivity", "Inside sleep window onCreate, redirecting to LockScreenActivity")
            val intent = Intent(this, LockScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }

        setContentView(R.layout.activity_main)
        
        sharedPreferences = getSharedPreferences("SleepEnforcerPrefs", MODE_PRIVATE)
        
        statusLabel = findViewById(R.id.statusLabel)
        settingsButton = findViewById(R.id.settingsButton)
        
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        requestRequiredPermissions()
    }

    private fun requestRequiredPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        // Check POST_NOTIFICATIONS (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
                Log.w("MainActivity", "POST_NOTIFICATIONS permission not granted")
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            Log.d("MainActivity", "Requesting permissions: $permissionsNeeded")
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_CODE)
        } else {
            checkSpecialPermissions()
        }
    }

    private fun checkSpecialPermissions() {
        // 1. Check SYSTEM_ALERT_WINDOW (Draw over other apps)
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "⚠️ Please grant Overlay Permission to enforce lock screen past bedtime.", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
            return
        }

        // 2. Check SCHEDULE_EXACT_ALARM (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "⚠️ Please grant Exact Alarm permission for precise sleep checks.", Toast.LENGTH_LONG).show()
                val intent = Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                return
            }
        }

        refreshData()
    }

    private fun refreshData() {
        val scheduleManager = SleepScheduleManager(this)

        warningHour = scheduleManager.getActiveWarningHour()
        warningMinute = scheduleManager.getActiveWarningMinute()
        shutdownHour = scheduleManager.getActiveShutdownHour()
        shutdownMinute = scheduleManager.getActiveShutdownMinute()

        val warningTime = String.format("%02d:%02d", warningHour, warningMinute)
        val shutdownTime = String.format("%02d:%02d", shutdownHour, shutdownMinute)
        val wakeTime = String.format("%02d:%02d", scheduleManager.wakeHour, scheduleManager.wakeMinute)

        val warningText = getString(R.string.warning_at, warningTime)
        val shutdownText = getString(R.string.shutdown_at, shutdownTime)
        val wakeText = "🌞 Wake Time: $wakeTime"

        if (scheduleManager.isExtensionActive) {
            val extensionText = "⚡ 30-Min Late-Night Extension Active!"
            statusLabel.text = "$extensionText\n\n$warningText\n$shutdownText\n$wakeText"
        } else {
            statusLabel.text = "$warningText\n$shutdownText\n$wakeText"
        }

        // Schedule alarms via AlarmUtils
        AlarmUtils.scheduleSleepCheck(this)
        Log.d("MainActivity", "Sleep check alarms successfully scheduled!")
    }

    override fun onResume() {
        super.onResume()
        Log.d("MainActivity", "onResume: Checking special permissions and data refresh")
        
        val scheduleManager = SleepScheduleManager(this)
        if (scheduleManager.isInsideSleepWindow()) {
            Log.d("MainActivity", "Inside sleep window onResume, redirecting to LockScreenActivity")
            val intent = Intent(this, LockScreenActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        
        // Ensure POST_NOTIFICATIONS is already granted if needed
        var canPostNotif = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            canPostNotif = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        }

        if (canPostNotif) {
            checkSpecialPermissions()
        } else {
            requestRequiredPermissions()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        Log.d("MainActivity", "onRequestPermissionsResult: requestCode=$requestCode, permissions=${permissions.joinToString()}, results=${grantResults.joinToString()}")
        
        var allGranted = true
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false
                break
            }
        }
        
        if (allGranted) {
            Log.d("MainActivity", "All standard permissions granted, checking special permissions")
            checkSpecialPermissions()
        } else {
            Toast.makeText(this, "Standard notification permissions were denied.", Toast.LENGTH_LONG).show()
        }
    }
}
