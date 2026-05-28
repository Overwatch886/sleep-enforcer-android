---
name: run-android-lint
description: >-
  Execute Gradle Android Lint task to analyze static code quality, verify resource references, 
  and find styling issues or structural errors in the Android app.
metadata:
  version: "1.0"
  author: Antigravity Agent
  workspace: c:\developer\healthy_sleep_enforcer\andriod_app
---
# Run Android Lint Checks

## Overview
This skill executes the static code analysis suite (`lint`) provided by the Android Gradle plugin. Lint inspects Android project source files for potential bugs and optimization improvements for security, performance, usability, accessibility, and internationalization.

## Execution Steps

Execute the following Gradle command in the Android workspace directory `c:\developer\healthy_sleep_enforcer\andriod_app`:

```powershell
./gradlew lint
```

## Output Verification
- Verify the task completes and check the generated report for any warnings/errors.
- By default, report files are written to:
  - HTML Report: `app/build/reports/lint-results-debug.html`
  - XML Report: `app/build/reports/lint-results-debug.xml`
- Fix any critical errors found in the layout or resource XML files or Kotlin files before building release binaries.
