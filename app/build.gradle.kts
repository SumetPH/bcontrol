plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.sumetph.bcontrol"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sumetph.bcontrol"
        minSdk = 29
        // Must stay <= 22: SettingsProvider rejects non-public system keys (hw_screen_temp_brightness)
        // for apps targeting 23+, even with WRITE_SECURE_SETTINGS. Android 14+ refuses to install it.
        targetSdk = 22
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}
