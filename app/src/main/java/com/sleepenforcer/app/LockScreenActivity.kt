package com.sleepenforcer.app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import android.os.Handler
import android.os.Looper

class LockScreenActivity : AppCompatActivity() {

    private lateinit var lockScreenText: TextView
    private lateinit var submitReasonButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var scheduleManager: SleepScheduleManager
    private val handler = Handler(Looper.getMainLooper())
    private var enforceRunnable: Runnable? = null
    private var isEnforceLoopRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lock_screen)

        sharedPreferences = getSharedPreferences("SleepEnforcerPrefs", MODE_PRIVATE)
        scheduleManager = SleepScheduleManager(this)

        lockScreenText = findViewById(R.id.lock_screen_text)
        submitReasonButton = findViewById(R.id.submitReasonButton)

        // Make activity show over secure keyguard/lock screen and keep screen on
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        }

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }

        submitReasonButton.setOnClickListener {
            val intent = Intent(this, ReasonActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Ignore back key to prevent escape
            }
        })

        enforceLockState()
        setupEnforceLoop()
    }

    private fun setupEnforceLoop() {
        enforceRunnable = object : Runnable {
            override fun run() {
                if (scheduleManager.isInsideSleepWindow()) {
                    if (!isFinishing && !isDestroyed && !hasWindowFocus()) {
                        val intent = Intent(this@LockScreenActivity, LockScreenActivity::class.java).apply {
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

    private fun enforceLockState() {
        // If we are mathematically outside the sleep window, unlock!
        if (!scheduleManager.isInsideSleepWindow()) {
            Log.d("LockScreenActivity", "Outside sleep window. Auto-unlocking...")
            finish()
            return
        }

        val wakeTimeStr = String.format("%02d:%02d", scheduleManager.wakeHour, scheduleManager.wakeMinute)
        
        // If they exhausted all attempts in ReasonActivity, trigger a complete lockdown!
        val isLockoutActive = sharedPreferences.getBoolean("lockout_active", false)
        if (isLockoutActive) {
            lockScreenText.text = "💤 SLEEP MODE ACTIVE\n\nAccess Blocked until $wakeTimeStr"
            lockScreenText.textSize = 28f
            submitReasonButton.visibility = View.GONE // Hide reason button entirely!
        } else {
            lockScreenText.text = "🛑 GO TO SLEEP"
            submitReasonButton.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d("LockScreenActivity", "onResume: Checking lock state")
        enforceLockState()
        setupEnforceLoop()
    }

    override fun onPause() {
        super.onPause()
        // If we are still inside the sleep window, restart ourselves to overlay and stay on top!
        if (scheduleManager.isInsideSleepWindow()) {
            Log.d("LockScreenActivity", "onPause: Forcing screen back to front")
            if (!isFinishing) {
                val intent = Intent(this, LockScreenActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                startActivity(intent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopEnforceLoop()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Double-guard block back key
    }
}
