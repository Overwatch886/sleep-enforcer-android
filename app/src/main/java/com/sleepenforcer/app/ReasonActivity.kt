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
import android.widget.LinearLayout
import android.widget.ScrollView
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class ReasonActivity : AppCompatActivity() {
    
    private lateinit var titleLabel: TextView
    private lateinit var chatHistoryLabel: TextView
    private lateinit var chatScrollView: ScrollView
    private lateinit var reasonEdit: EditText
    private lateinit var submitButton: Button
    private lateinit var actionButtonsLayout: LinearLayout
    private lateinit var acceptButton: Button
    private lateinit var declineButton: Button
    
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
    private var enforceRunnable: Runnable? = null
    private var isEnforceLoopRunning = false
    private var chatHistory = "Coach: Hey, it's getting late. What are you working on?\n"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reason)
        
        titleLabel = findViewById(R.id.titleLabel)
        chatHistoryLabel = findViewById(R.id.chatHistoryLabel)
        chatScrollView = findViewById(R.id.chatScrollView)
        reasonEdit = findViewById(R.id.reasonEdit)
        submitButton = findViewById(R.id.submitButton)
        actionButtonsLayout = findViewById(R.id.actionButtonsLayout)
        acceptButton = findViewById(R.id.acceptButton)
        declineButton = findViewById(R.id.declineButton)
        
        val scheduleManager = SleepScheduleManager(this)
        val shutdownTime = String.format("%02d:%02d", scheduleManager.shutdownHour, scheduleManager.shutdownMinute)
        
        titleLabel.text = getString(R.string.time_for_bed, shutdownTime)
        chatHistoryLabel.text = chatHistory

        acceptButton.setOnClickListener {
            handleAcceptPlan()
        }

        declineButton.setOnClickListener {
            handleDeclinePlan()
        }
        
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
        setupEnforceLoop()
        startEnforceLoop()

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
        val reason = reasonEdit.text.toString().trim()
        
        if (reason.isEmpty()) {
            Toast.makeText(this, "Please enter what you are working on.", Toast.LENGTH_SHORT).show()
            return
        }

        // Append user response
        chatHistory += "\nYou: $reason\n"
        chatHistoryLabel.text = chatHistory
        reasonEdit.text.clear()
        scrollToBottom()

        // Hide input area, show loading...
        reasonEdit.visibility = View.GONE
        submitButton.visibility = View.GONE

        // Cancel grace period timer during coach processing
        graceTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        
        // Simulate AI thinking
        handler.postDelayed({
            generateCoachResponse(reason.lowercase())
        }, 1500)
    }

    private fun generateCoachResponse(input: String) {
        var subject = "that"
        val keywords = listOf("coding", "programming", "assignment", "homework", "deadline", "project", "studying", "reading", "working", "essay", "meeting", "code")
        for (keyword in keywords) {
            if (input.contains(keyword)) {
                subject = keyword
                break
            }
        }
        
        val plan = if (subject == "that") {
            "I see you're busy, but sleep is crucial. If you sleep now, you can wake up 1 hour earlier with a fresh mind to finish your work. Want to try that?"
        } else {
            "I understand $subject is important, but your brain needs rest to be productive. How about you sleep now, and wake up 1 hour earlier to tackle your $subject with renewed energy?"
        }
        
        chatHistory += "\nCoach: $plan\n"
        chatHistoryLabel.text = chatHistory
        scrollToBottom()

        actionButtonsLayout.visibility = View.VISIBLE
    }

    private fun handleAcceptPlan() {
        chatHistory += "\nYou: Accept\n"
        chatHistoryLabel.text = chatHistory

        // Lock immediately
        showCountdown()
    }

    private fun handleDeclinePlan() {
        chatHistory += "\nYou: Decline\n"
        chatHistoryLabel.text = chatHistory

        grantExtension()
    }

    private fun scrollToBottom() {
        chatScrollView.post {
            chatScrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun setupEnforceLoop() {
        val scheduleManager = SleepScheduleManager(this)
        enforceRunnable = object : Runnable {
            override fun run() {
                if (scheduleManager.isInsideSleepWindow()) {
                    if (!isFinishing && !isDestroyed && !hasWindowFocus()) {
                        val intent = Intent(this@ReasonActivity, ReasonActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                        }
                        startActivity(intent)
                    }
                    handler.postDelayed(this, 1000)
                }
            }
        }
    }

    private fun startEnforceLoop() {
        if (!isEnforceLoopRunning) {
            isEnforceLoopRunning = true
            enforceRunnable?.let { handler.post(it) }
        }
    }

    private fun stopEnforceLoop() {
        isEnforceLoopRunning = false
        enforceRunnable?.let { handler.removeCallbacks(it) }
        enforceRunnable = null
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
    
    private fun showCountdown() {
        val intent = Intent(this, CountdownActivity::class.java)
        startActivity(intent)
        finish()
    }
    
    override fun onResume() {
        super.onResume()
        startEnforceLoop()
    }

    override fun onDestroy() {
        super.onDestroy()
        graceTimeoutRunnable?.let {
            handler.removeCallbacks(it)
        }
        stopEnforceLoop()
    }
}
