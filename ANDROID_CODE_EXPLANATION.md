# 📱 Android Sleep Enforcer - Complete Code Explanation

## Overview
This document explains every line of code in the Android version of the Sleep Enforcer app. The Android app works similarly to the Windows version but uses Android-specific components.

---

## **Project Structure**

```
andriod_app/
├── app/
│   ├── build.gradle          # App build configuration
│   └── src/main/
│       ├── AndroidManifest.xml    # App configuration
│       ├── java/com/sleepenforcer/app/
│       │   ├── MainActivity.kt        # Main screen
│       │   ├── SettingsActivity.kt    # Settings screen
│       │   ├── ReasonActivity.kt      # Reason prompt screen
│       │   ├── CountdownActivity.kt   # Countdown screen
│       │   ├── TimeCheckService.kt    # Background service
│       │   └── BootReceiver.kt        # Boot receiver
│       └── res/
│           ├── layout/               # UI layouts (XML)
│           ├── values/               # Strings, colors, themes
│           └── xml/                  # Other XML configs
├── build.gradle              # Project build config
└── settings.gradle           # Project settings
```

---

## **1. MainActivity.kt - Main Screen**

### **Package Declaration**
```kotlin
package com.sleepenforcer.app
```
- **What it does:** Declares which package this file belongs to
- **Why:** Organizes code into groups (like folders)
- **Think of it as:** The file's "address" in the project

### **Import Statements**
```kotlin
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
```
- **What they do:** Bring in code from Android libraries
- **Intent:** Used to start other activities (screens)
- **SharedPreferences:** Android's way to save simple data (like settings)
- **Bundle:** Container for passing data between activities
- **Button/TextView:** UI widgets (visual elements)
- **AppCompatActivity:** Base class for activities (screens)
- **ContextCompat:** Helper for starting services

### **Class Declaration**
```kotlin
class MainActivity : AppCompatActivity() {
```
- **What it does:** Creates a class that extends AppCompatActivity
- **AppCompatActivity:** Provides features like action bars, themes
- **Think of it as:** A "screen" in your app

### **Class Properties**
```kotlin
private lateinit var statusLabel: TextView
private lateinit var settingsButton: Button
private lateinit var sharedPreferences: SharedPreferences
private var warningTime: String = "21:55"
private var shutdownTime: String = "22:00"
```
- **lateinit:** "I'll initialize this later, I promise"
- **private:** Only this class can access these
- **var:** Variable (can change)
- **val:** Constant (can't change)

### **onCreate() Method**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
```
- **override:** Replaces parent class's method
- **onCreate():** Called when activity is first created
- **super.onCreate():** Calls parent's onCreate (required)
- **setContentView():** Sets which layout file to use

```kotlin
    sharedPreferences = getSharedPreferences("SleepEnforcerPrefs", MODE_PRIVATE)
```
- **getSharedPreferences():** Gets a SharedPreferences object
- **"SleepEnforcerPrefs":** Name of the preferences file
- **MODE_PRIVATE:** Only this app can access it

```kotlin
    loadSettings()
    statusLabel = findViewById(R.id.statusLabel)
    settingsButton = findViewById(R.id.settingsButton)
```
- **loadSettings():** Loads saved times
- **findViewById():** Finds a widget by its ID (from XML)
- **R.id.statusLabel:** Reference to widget with id="statusLabel"

```kotlin
    updateStatusLabel()
    settingsButton.setOnClickListener {
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }
```
- **setOnClickListener:** Code to run when button is clicked
- **Intent:** Message to start another activity
- **SettingsActivity::class.java:** Reference to SettingsActivity class
- **startActivity():** Opens the SettingsActivity

### **onResume() Method**
```kotlin
override fun onResume() {
    super.onResume()
    loadSettings()
    updateStatusLabel()
}
```
- **onResume():** Called when activity becomes visible
- **Why reload?** Settings might have changed in SettingsActivity

### **loadSettings() Method**
```kotlin
private fun loadSettings() {
    warningTime = sharedPreferences.getString("warning_time", "21:55") ?: "21:55"
    shutdownTime = sharedPreferences.getString("shutdown_time", "22:00") ?: "22:00"
}
```
- **getString():** Gets a string value from SharedPreferences
- **"warning_time":** Key to look up
- **"21:55":** Default value if key doesn't exist
- **?: "21:55":** If null, use "21:55" (Kotlin null-safety)

### **updateStatusLabel() Method**
```kotlin
private fun updateStatusLabel() {
    val warningText = getString(R.string.warning_at, warningTime)
    val shutdownText = getString(R.string.shutdown_at, shutdownTime)
    statusLabel.text = "$warningText\n$shutdownText"
}
```
- **getString():** Gets string from strings.xml
- **R.string.warning_at:** Reference to string resource
- **warningTime:** Value to insert (replaces %1$s)
- **$variable:** String interpolation (inserts variable into string)

---

## **2. SettingsActivity.kt - Settings Screen**

### **Class Properties**
```kotlin
private lateinit var warningTimeEdit: EditText
private lateinit var shutdownTimeEdit: EditText
private lateinit var saveButton: Button
```
- **EditText:** Text input field (like a text box)

### **onCreate() Method**
```kotlin
warningTimeEdit.setText(currentWarningTime)
shutdownTimeEdit.setText(currentShutdownTime)
```
- **setText():** Sets the text in the input field

### **saveSettings() Method**
```kotlin
private fun saveSettings() {
    val newWarningTime = warningTimeEdit.text.toString().trim()
    val newShutdownTime = shutdownTimeEdit.text.toString().trim()
```
- **.text:** Gets the Editable object
- **.toString():** Converts to String
- **.trim():** Removes spaces from start/end

```kotlin
    if (!isValidTimeFormat(newWarningTime)) {
        Toast.makeText(this, getString(R.string.invalid_time_format), Toast.LENGTH_SHORT).show()
        return
    }
```
- **isValidTimeFormat():** Checks if time is in HH:MM format
- **Toast:** Small popup message
- **return:** Exits function early

```kotlin
    val editor = sharedPreferences.edit()
    editor.putString("warning_time", newWarningTime)
    editor.putString("shutdown_time", newShutdownTime)
    editor.apply()
```
- **edit():** Gets editor to write data
- **putString():** Saves a string value
- **apply():** Saves asynchronously (doesn't block)

### **isValidTimeFormat() Method**
```kotlin
private fun isValidTimeFormat(time: String): Boolean {
    val timePattern = Regex("^([0-1][0-9]|2[0-3]):[0-5][0-9]$")
    return timePattern.matches(time)
}
```
- **Regex:** Regular expression (pattern matching)
- **^:** Start of string
- **([0-1][0-9]|2[0-3]):** Hours 00-23
- **:[0-5][0-9]$:** Minutes 00-59
- **matches():** Checks if string matches pattern

---

## **3. ReasonActivity.kt - Reason Prompt Screen**

### **Class Properties**
```kotlin
private val handler = Handler(Looper.getMainLooper())
private var graceTimeoutRunnable: Runnable? = null
private val validReasons = listOf("client deadline", "assignment due", "emergency", "meeting")
private val gracePeriodMs = 180_000L
```
- **Handler:** Runs code on main thread after delay
- **Runnable:** Piece of code to run later
- **listOf():** Creates a list
- **180_000L:** 180,000 milliseconds (3 minutes) - L means Long

### **onCreate() Method**
```kotlin
reasonEdit.setOnEditorActionListener { _, actionId, event ->
    if (actionId == EditorInfo.IME_ACTION_DONE || 
        (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
        checkReason()
        true
    } else {
        false
    }
}
```
- **setOnEditorActionListener:** Called when keyboard action happens
- **IME_ACTION_DONE:** User pressed "Done" on keyboard
- **KEYCODE_ENTER:** User pressed Enter key
- **true/false:** Whether we handled the event

```kotlin
reasonEdit.requestFocus()
startGracePeriod()
```
- **requestFocus():** Puts cursor in the text field
- **startGracePeriod():** Starts 3-minute timer

### **startGracePeriod() Method**
```kotlin
private fun startGracePeriod() {
    graceTimeoutRunnable = Runnable {
        if (!isFinishing && !isDestroyed) {
            showCountdown()
        }
    }
    handler.postDelayed(graceTimeoutRunnable!!, gracePeriodMs)
}
```
- **Runnable:** Code to run later
- **isFinishing:** Is activity closing?
- **isDestroyed:** Is activity destroyed?
- **postDelayed():** Run after delay (180 seconds)

### **checkReason() Method**
```kotlin
private fun checkReason() {
    graceTimeoutRunnable?.let {
        handler.removeCallbacks(it)
    }
    val reason = reasonEdit.text.toString().lowercase().trim()
    val isValid = validReasons.any { validKeyword ->
        reason.contains(validKeyword, ignoreCase = true)
    }
```
- **?.let:** If not null, do this
- **removeCallbacks():** Cancels scheduled runnable
- **lowercase():** Converts to lowercase
- **any():** Returns true if any element matches
- **contains():** Checks if string contains substring

---

## **4. CountdownActivity.kt - Countdown Screen**

### **updateCountdown() Method**
```kotlin
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
        enterSleepMode()
    }
}
```
- **Recursive function:** Calls itself
- **postDelayed(..., 1000):** Run after 1 second
- **remainingSeconds--:** Decrease by 1

---

## **5. TimeCheckService.kt - Background Service**

### **Class Declaration**
```kotlin
class TimeCheckService : Service() {
```
- **Service:** Base class for background services

### **onCreate() Method**
```kotlin
createNotificationChannel()
startForeground(NOTIFICATION_ID, createNotification())
startTimeChecking()
```
- **createNotificationChannel():** Creates channel (Android 8.0+)
- **startForeground():** Starts as foreground service (shows notification)
- **startTimeChecking():** Starts time checking loop

### **onStartCommand() Method**
```kotlin
override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    return START_STICKY
}
```
- **START_STICKY:** Service restarts if killed by Android

### **createNotificationChannel() Method**
```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    val channel = NotificationChannel(CHANNEL_ID, "Sleep Enforcer", NotificationManager.IMPORTANCE_LOW)
    val notificationManager = getSystemService(NotificationManager::class.java)
    notificationManager.createNotificationChannel(channel)
}
```
- **Build.VERSION.SDK_INT:** Current Android version
- **VERSION_CODES.O:** Android 8.0 (API 26)
- **NotificationChannel:** Required for Android 8.0+
- **IMPORTANCE_LOW:** Low priority (no sound)

### **checkTime() Method**
```kotlin
private fun checkTime() {
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    val warningTime = sharedPreferences.getString("warning_time", "21:55") ?: "21:55"
    val shutdownTime = sharedPreferences.getString("shutdown_time", "22:00") ?: "22:00"
    
    if (currentTime == warningTime) {
        showWarningNotification()
    }
    if (currentTime == shutdownTime) {
        openReasonActivity()
    }
}
```
- **SimpleDateFormat:** Formats date/time
- **"HH:mm":** Format string (24-hour time)
- **Date():** Current date/time
- **format():** Converts to string

### **openReasonActivity() Method**
```kotlin
private fun openReasonActivity() {
    val intent = Intent(this, ReasonActivity::class.java)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    startActivity(intent)
}
```
- **FLAG_ACTIVITY_NEW_TASK:** Required when starting activity from service

---

## **6. BootReceiver.kt - Boot Receiver**

### **Class Declaration**
```kotlin
class BootReceiver : BroadcastReceiver() {
```
- **BroadcastReceiver:** Listens for system broadcasts

### **onReceive() Method**
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
        val serviceIntent = Intent(context, TimeCheckService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
```
- **onReceive():** Called when broadcast is received
- **ACTION_BOOT_COMPLETED:** System finished booting
- **startForegroundService():** Starts the service

---

## **7. Layout XML Files**

### **Basic Structure**
```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">
```
- **ConstraintLayout:** Layout manager (positions widgets)
- **match_parent:** Fill available space

### **TextView Example**
```xml
<TextView
    android:id="@+id/titleLabel"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/app_active"
    android:textSize="18sp" />
```
- **android:id:** Unique ID (used in findViewById)
- **wrap_content:** Size fits content
- **@string/app_active:** Reference to string resource
- **sp:** Scale-independent pixels (scales with font size)

### **Button Example**
```xml
<Button
    android:id="@+id/settingsButton"
    android:text="@string/settings_button"
    android:backgroundTint="@color/blue_500" />
```
- **backgroundTint:** Button background color
- **@color/blue_500:** Reference to color resource

### **EditText Example**
```xml
<EditText
    android:id="@+id/warningTimeEdit"
    android:hint="21:55"
    android:inputType="time" />
```
- **hint:** Placeholder text (gray, disappears when typing)
- **inputType:** Keyboard type (time shows time picker)

### **Constraints**
```xml
app:layout_constraintTop_toBottomOf="@id/titleLabel"
app:layout_constraintStart_toStartOf="parent"
app:layout_constraintEnd_toEndOf="parent"
```
- **constraintTop_toBottomOf:** Position below titleLabel
- **constraintStart_toStartOf:** Align to left
- **constraintEnd_toEndOf:** Align to right

---

## **8. AndroidManifest.xml**

### **Permissions**
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```
- **RECEIVE_BOOT_COMPLETED:** Start service after boot
- **SCHEDULE_EXACT_ALARM:** Set exact alarms
- **POST_NOTIFICATIONS:** Show notifications (Android 13+)

### **Activity Declaration**
```xml
<activity
    android:name=".MainActivity"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```
- **android:name:** Activity class name
- **exported="true":** Other apps can launch it
- **MAIN/LAUNCHER:** Makes it the launcher activity (appears in app drawer)

### **Service Declaration**
```xml
<service
    android:name=".TimeCheckService"
    android:enabled="true"
    android:exported="false" />
```
- **enabled="true":** Service is enabled
- **exported="false":** Only this app can start it

---

## **9. build.gradle Files**

### **App build.gradle**
```gradle
plugins {
    id 'com.android.application'
    id 'org.jetbrains.kotlin.android'
}
```
- **com.android.application:** Makes it an Android app
- **kotlin.android:** Enables Kotlin

```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 26
        targetSdk 34
    }
}
```
- **compileSdk:** Android version to compile against
- **minSdk:** Minimum Android version that can run app
- **targetSdk:** Android version you're targeting

```gradle
dependencies {
    implementation 'androidx.core:core-ktx:1.12.0'
    implementation 'androidx.appcompat:appcompat:1.6.1'
}
```
- **dependencies:** External libraries to use
- **implementation:** Include in app

---

## **Key Android Concepts**

### **Activities**
- A "screen" in your app
- Each activity has a layout (UI) and code (logic)
- Activities can start other activities

### **Services**
- Code that runs in the background
- Foreground services show a notification
- Continue running even when app is closed

### **SharedPreferences**
- Simple key-value storage
- Like a small database
- Persists between app launches

### **Intents**
- Messages to start activities/services
- Can pass data between components

### **Layouts (XML)**
- Define UI structure
- Separate from code (separation of concerns)
- Easier to modify without changing code

### **Resources**
- Strings, colors, themes stored in XML
- Makes translation easier
- Centralized styling

---

## **App Flow**

1. **App starts** → MainActivity opens
2. **TimeCheckService starts** → Checks time every minute
3. **At warning_time** → Shows notification
4. **At shutdown_time** → Opens ReasonActivity
5. **User enters reason** → Validates against keywords
   - **Valid** → Grants 30-minute extension
   - **Invalid** → Opens CountdownActivity
6. **After 60 seconds** → Enters sleep mode
7. **User can click buttons** → Navigate between screens

---

## **Differences from Windows Version**

1. **No hibernation:** Android can't hibernate like Windows
   - Instead, we close the app (finishAffinity())
   - Could use DevicePolicyManager for screen lock (requires admin permission)

2. **Background service:** Uses Service instead of threading
   - More reliable on Android
   - Can survive app closure

3. **Notifications:** Shows notifications instead of messageboxes
   - Better for mobile UX
   - Less intrusive

4. **Activities vs Frames:** Uses Activities instead of Tkinter frames
   - Each screen is a separate Activity
   - Android manages the lifecycle

5. **SharedPreferences vs variables:** Saves settings persistently
   - Settings survive app restarts
   - Windows version uses in-memory variables

---

## **Testing the App**

1. **Open Android Studio**
2. **Open the project** (andriod_app folder)
3. **Connect Android device** or start emulator
4. **Click Run** (green play button)
5. **App installs and runs**

---

## **Common Issues & Solutions**

### **App crashes on start**
- Check AndroidManifest.xml for missing activities
- Check that all layout files exist

### **Service doesn't start**
- Check permissions in AndroidManifest.xml
- Check that BootReceiver is registered

### **Time checking doesn't work**
- Check that service is running (notification should appear)
- Check SharedPreferences for saved times

### **Layout looks wrong**
- Check that all IDs match between XML and Kotlin
- Check that constraints are set correctly

---

## **Next Steps**

1. **Add device admin permission** for actual screen lock
2. **Add WorkManager** for more reliable background tasks
3. **Add database** for storing reason history
4. **Add statistics** (how many times extended, etc.)
5. **Improve UI** with Material Design 3 components

---

This completes the Android version! The app should work similarly to the Windows version, adapted for Android's architecture and best practices.


