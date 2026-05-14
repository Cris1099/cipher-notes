plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "dev.cipher.notes"
    compileSdk = 35

    defaultConfig {
        applicationId = "dev.cipher.notes"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // No telemetry metadata in APK
    dependenciesInfo {
        includeInApk = false
        includeInBundle = false
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.security.crypto)
    implementation(libs.coroutines.android)
    implementation(libs.datastore.prefs)
    implementation(libs.core.ktx)
    implementation(libs.splashscreen)

    // Crypto: Tink (Google AEAD library, auditable)
    implementation("com.google.crypto.tink:tink-android:1.10.0")

    // Serialization: kotlinx-serialization (FOSS, JetBrains)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Logging: Timber (FOSS, Jake Wharton)
    implementation("com.jakewharton.timber:timber:5.0.1")

    debugImplementation(libs.compose.ui.tooling)
}
