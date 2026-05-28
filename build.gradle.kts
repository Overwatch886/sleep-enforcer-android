

// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    val kotlinversion by extra("1.9.22") // Kotlin syntax for extra properties

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        // Kotlin syntax requires parentheses and double quotes

        classpath("com.android.tools.build:gradle:8.13.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinversion")
    }
}

// In Kotlin DSL, we usually register tasks like this
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
