package com.sleepenforcer.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class ReasonActivity : AppCompatActivity() {
    
    private lateinit var titleLabel: TextView
    private lateinit var attemptsLabel: TextView
    private lateinit var reasonEdit: EditText
    private lateinit var submitButton: Button
    
    private val handler = Handler(Looper.getMainLooper())
    private var graceTimeoutRunnable: Runnable? = null
    
    private val validReasons = listOf(
        "deadline",
        "assignment",
        "due",
        "emergency",
        "meeting"
    )
    
    private val gracePeriodMs = 180_000L // 3 minutes
    private var remainingAttempts = 3
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reason)
        
        titleLabel = findViewById(R.id.titleLabel)
        attemptsLabel = findViewById(R.id.attemptsLabel)
        reasonEdit = findViewById(R.id.reasonEdit)
        submitButton = findViewById(R.id.submitButton)
        
        val scheduleManager = SleepScheduleManager(this)
        val shutdownTime = String.format("%02d:%02d", scheduleManager.shutdownHour, scheduleManager.shutdownMinute)
        
        titleLabel.text = getString(R.string.time_for_bed, shutdownTime)
        attemptsLabel.text = "Attempts remaining: $remainingAttempts"
        reasonEdit.hint = getString(R.string.enter_reason_hint)
        
        submitButton.setOnClickListener {
            checkReason()
        }
        
        reasonEdit.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || 
                (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                checkReason()
                true
            } else {
                false
            }
        }
        
        reasonEdit.requestFocus()
        startGracePeriod()

        // Disable standard back button gesture
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Blocked
            }
        })
    }
    
    private fun startGracePeriod() {
        graceTimeoutRunnable = Runnable {
            if (!isFinishing && !isDestroyed) {
                showCountdown()
            }
        }
        handler.postDelayed(graceTimeoutRunnable!!, gracePeriodMs)
    }
    
    private fun checkReason() {
        val reason = reasonEdit.text.toString().lowercase().trim()
        
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter a reason first.", Toast.LENGTH_SHORT).show()
            return
        }

        // Cancel grace period timer
        graceTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        
        val isValid = validReasons.any { validKeyword ->
            reason.contains(validKeyword, ignoreCase = true)
        }
        
        if (isValid) {
            grantExtension()
        } else {
            handleInvalidReason()
        }
        
        reasonEdit.text.clear()
    }
    
    private fun grantExtension() {
        val scheduleManager = SleepScheduleManager(this)
        
        // Write standard extension settings
        scheduleManager.grantExtension(30)
        
        // Re-schedule exact Alarms immediately using updated times!
        AlarmUtils.scheduleSleepCheck(this)
        
        // Send a system notification so the user sees it in their drawer and as a heads-up
        val extTime = String.format("%02d:%02d", scheduleManager.getActiveShutdownHour(), scheduleManager.getActiveShutdownMinute())
        sendExtensionNotification(extTime)
        
        Toast.makeText(
            this,
            getString(R.string.extension_granted, 30),
            Toast.LENGTH_LONG
        ).show()
        
        // Return to MainActivity
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun sendExtensionNotification(extendedBedtime: String) {
        val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        val channelId = "SleepEnforcerChannel"
        
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Sleep Enforcer Warnings",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Warning notifications before the device locks."
                enableVibration(true)
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = androidx.core.app.NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("⏰ Sleep Extension Granted")
            .setContentText("A 30-minute extension has been granted. New bedtime is $extendedBedtime.")
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
            .setCategory(androidx.core.app.NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(2, notification) // ID 2 for extension notification
    }
    
    private fun handleInvalidReason() {
        remainingAttempts--
        attemptsLabel.text = "Attempts remaining: $remainingAttempts"

        if (remainingAttempts > 0) {
            Toast.makeText(
                this,
                "That reason is not accepted. You have $remainingAttempts attempts left.",
                Toast.LENGTH_LONG
            ).show()
            
            // Restart grace period countdown
            startGracePeriod()
        } else {
            getSharedPreferences("SleepEnforcerPrefs", MODE_PRIVATE).edit()
                .putBoolean("lockout_active", true)
                .apply()
            showCountdown()
        }
    }

    private fun showCountdown() {
        val intent = Intent(this, CountdownActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        graceTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
}
