# 📱 Android Sleep Enforcer

An Android version of the Healthy Sleep Enforcer app that helps you maintain healthy sleep habits by prompting for a reason to stay up and encouraging sleep at bedtime.

## 📋 Features

- ⏰ 5-minute warning before shutdown time
- 🛑 Prompts for a valid reason to stay awake
- ✅ Grants 30-minute extension for valid reasons
- ⏱️ 60-second final countdown before sleep mode
- 🔔 Background service that runs even when app is closed
- ⚙️ Customizable warning and shutdown times

## 🛠️ Requirements

- **Android Studio** (latest version recommended)
- **Android SDK** (API 26 / Android 8.0 or higher)
- **Kotlin** (included with Android Studio)
- **Android device** or **emulator** for testing

## 📦 Installation & Setup

### 1. Install Android Studio

Download and install [Android Studio](https://developer.android.com/studio) if you haven't already.

### 2. Open the Project

1. Open Android Studio
2. Click **File → Open**
3. Navigate to the `andriod_app` folder
4. Click **OK**

### 3. Sync Gradle

Android Studio will automatically sync Gradle (download dependencies). Wait for it to finish.

### 4. Set Up Device/Emulator

**Option A: Physical Device**
1. Enable **Developer Options** on your Android device:
   - Go to **Settings → About Phone**
   - Tap **Build Number** 7 times
2. Enable **USB Debugging**:
   - Go to **Settings → Developer Options**
   - Enable **USB Debugging**
3. Connect device via USB
4. Allow USB debugging when prompted

**Option B: Emulator**
1. Click **Tools → Device Manager**
2. Click **Create Device**
3. Select a device (e.g., Pixel 5)
4. Download a system image (e.g., API 34)
5. Click **Finish**

### 5. Run the App

1. Click the **Run** button (green play icon) or press **Shift+F10**
2. Select your device/emulator
3. Wait for the app to install and launch

## 📚 Understanding the Code

For detailed explanations of every line of code, see:
- **[ANDROID_CODE_EXPLANATION.md](ANDROID_CODE_EXPLANATION.md)** - Complete line-by-line explanation

## 🏗️ Project Structure

```
andriod_app/
├── app/
│   ├── build.gradle              # App build configuration
│   └── src/main/
│       ├── AndroidManifest.xml   # App configuration & permissions
│       ├── java/com/sleepenforcer/app/
│       │   ├── MainActivity.kt        # Main screen
│       │   ├── SettingsActivity.kt    # Settings screen
│       │   ├── ReasonActivity.kt      # Reason prompt screen
│       │   ├── CountdownActivity.kt   # Countdown screen
│       │   ├── TimeCheckService.kt    # Background service
│       │   └── BootReceiver.kt        # Boot receiver
│       └── res/
│           ├── layout/               # UI layouts (XML files)
│           ├── values/               # Strings, colors, themes
│           └── xml/                  # Other XML configs
├── build.gradle              # Project build config
└── settings.gradle           # Project settings
```

## 🎯 How It Works

1. **App starts** → MainActivity shows current schedule
2. **Background service starts** → TimeCheckService checks time every minute
3. **At warning_time** → Shows notification (5 minutes before shutdown)
4. **At shutdown_time** → Opens ReasonActivity
5. **User enters reason** → Validates against keywords:
   - **Valid keywords:** "client deadline", "assignment due", "emergency", "meeting"
   - **Valid reason** → Grants 30-minute extension
   - **Invalid reason** → Shows CountdownActivity (60-second countdown)
6. **After countdown** → Enters sleep mode (closes app)

## ⚙️ Configuration

### Default Times
- **Warning Time:** 21:55 (5 minutes before shutdown)
- **Shutdown Time:** 22:00

### Changing Times
1. Open the app
2. Tap **Settings**
3. Enter new times in **HH:MM** format (e.g., 22:30)
4. Tap **Save Settings**

### Valid Reasons
The app accepts reasons containing these keywords:
- "client deadline"
- "assignment due"
- "emergency"
- "meeting"

## 🔧 Building for Release

### 1. Generate Signed APK

1. Click **Build → Generate Signed Bundle / APK**
2. Select **APK**
3. Create a new keystore (or use existing)
4. Fill in keystore details
5. Select **release** build variant
6. Click **Finish**

### 2. Install APK

Transfer the APK to your device and install it.

## 🐛 Troubleshooting

### App crashes on start
- Check that all activities are declared in AndroidManifest.xml
- Check that all layout files exist

### Service doesn't start
- Check permissions in AndroidManifest.xml
- Check that notification permission is granted (Android 13+)

### Time checking doesn't work
- Check that service is running (notification should appear)
- Check SharedPreferences for saved times
- Restart the app

### Layout looks wrong
- Check that all IDs match between XML and Kotlin code
- Check that constraints are set correctly in layout files

## 📝 Notes

- **Sleep Mode:** The Android version closes the app instead of hibernating (Android doesn't support hibernation like Windows)
- **Background Service:** The service runs in the background and shows a notification
- **Permissions:** The app requires notification permission on Android 13+
- **Battery Optimization:** You may need to disable battery optimization for the app to ensure the service runs reliably

## 🔄 Differences from Windows Version

1. **No hibernation** - Android can't hibernate like Windows
2. **Background service** - Uses Service instead of threading
3. **Notifications** - Shows notifications instead of messageboxes
4. **Activities** - Uses Activities instead of Tkinter frames
5. **Persistent storage** - Uses SharedPreferences instead of in-memory variables

## 📖 Learning Resources

- [Android Developer Guide](https://developer.android.com/guide)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Material Design](https://material.io/design)

## 🤝 Contributing

Feel free to improve the app! Some ideas:
- Add device admin permission for actual screen lock
- Add WorkManager for more reliable background tasks
- Add database for storing reason history
- Add statistics (how many times extended, etc.)
- Improve UI with Material Design 3 components

## 📄 License

Same license as the Windows version.

---

**Happy coding! 💤**


