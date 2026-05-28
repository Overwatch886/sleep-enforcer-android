---
name: build-android-release-apk
description: >-
  Compile a production release Android APK for the Sleep Enforcer mobile app using Gradle.
metadata:
  version: "1.0"
  author: Antigravity Agent
  workspace: c:\developer\healthy_sleep_enforcer\andriod_app
---
# Build Android Release APK

## Overview
This skill compiles the Android Sleep Enforcer application into an unsigned release APK (`app-release-unsigned.apk`) or a signed release APK using the Gradle build system.

## Prerequisites
- Android SDK installed and configured.
- Valid keystore and signing configurations (if signing is required).
- Current branch: `agent-dev`.

## Execution Steps

Execute the following Gradle task in the Android workspace directory `c:\developer\healthy_sleep_enforcer\andriod_app`:

```powershell
./gradlew assembleRelease
```

## Output Verification
- Verify the build completes successfully with `BUILD SUCCESSFUL`.
- The compiled APK will be generated at:
  `app/build/outputs/apk/release/app-release-unsigned.apk` (or `app-release.apk` if signing is fully configured in `app/build.gradle`).
