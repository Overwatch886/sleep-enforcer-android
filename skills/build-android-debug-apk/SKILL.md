---
name: build-android-debug-apk
description: >-
  Compile a debug Android APK for the Sleep Enforcer mobile app using Gradle.
metadata:
  version: "1.0"
  author: Antigravity Agent
  workspace: c:\developer\healthy_sleep_enforcer\andriod_app
---
# Build Android Debug APK

## Overview
This skill compiles the Android Sleep Enforcer application (`andriod_app`) into a debug APK (`app-debug.apk`) using the local Gradle wrapper.

## Prerequisites
- Android SDK installed and configured.
- Gradle configured (either globally or via gradle-wrapper).
- Current branch: `agent-dev` (or designated development branch).

## Execution Steps

Run the following command in the Android workspace directory `c:\developer\healthy_sleep_enforcer\andriod_app`:

```powershell
./gradlew assembleDebug
```

*(Note: On Windows, use `gradlew.bat assembleDebug` if using a standard Command Prompt, or `./gradlew assembleDebug` in PowerShell)*

## Output Verification
- Verify the build completes successfully with `BUILD SUCCESSFUL`.
- The compiled APK will be generated at:
  `app/build/outputs/apk/debug/app-debug.apk`
