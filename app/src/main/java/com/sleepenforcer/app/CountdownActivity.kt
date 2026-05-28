package com.sleepenforcer.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class CountdownActivity : AppCompatActivity() {
    
    private lateinit var countdownLabel: TextView
    private lateinit var hibernateButton: Button
    private lateinit var reasonButton: Button
    
    private val handler = Handler(Looper.getMainLooper())
    private var countdownRunnable: Runnable? = null
    private var remainingSeconds = 60
    private var isCountdownActive = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_countdown)
        
        countdownLabel = findViewById(R.id.countdownLabel)
        hibernateButton = findViewById(R.id.hibernateButton)
        reasonButton = findViewById(R.id.reasonButton)
        
        hibernateButton.setOnClickListener {
            enterSleepMode()
        }
        
        reasonButton.setOnClickListener {
            goToReasonScreen()
        }
        
        startCountdown()

        // Disable standard back button gesture to prevent lock evasion
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Blocked
            }
        })
    }
    
    private fun startCountdown() {
        remainingSeconds = 60
        isCountdownActive = true
        updateCountdown()
    }
    
    private fun updateCountdown() {
        if (!isCountdownActive || isFinishing || isDestroyed) {
            return
        }
        
        if (remainingSeconds > 0) {
            countdownLabel.text = getString(R.string.hibernate_countdown, remainingSeconds)
            remainingSeconds--
            
            countdownRunnable = Runnable {
                updateCountdown()
            }
            handler.postDelayed(countdownRunnable!!, 1000)
        } else {
            countdownLabel.text = getString(R.string.hibernating)
            enterSleepMode()
        }
    }
    
    private fun enterSleepMode() {
        isCountdownActive = false
        
        countdownRunnable?.let {
            handler.removeCallbacks(it)
        }
        
        // Launch LockScreenActivity (singleInstance task)
        val intent = Intent(this, LockScreenActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        
        // Close the rest of the application
        finishAffinity()
    }
    
    private fun goToReasonScreen() {
        isCountdownActive = false
        
        countdownRunnable?.let {
            handler.removeCallbacks(it)
        }

        // Return to ReasonActivity
        val intent = Intent(this, ReasonActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        isCountdownActive = false
        countdownRunnable?.let {
            handler.removeCallbacks(it)
        }
    }
}
