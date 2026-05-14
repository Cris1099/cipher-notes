# Cipher — Dependencies

All libraries used in Cipher are FOSS (Free and Open Source Software).
This document lists every dependency with source and license.

## Build & Gradle

| Library | Version | License | Source |
|---------|---------|---------|--------|
| Android Gradle Plugin | 8.5.2 | Apache 2.0 | [google/android-gradle-plugin](https://source.android.com) |
| Kotlin | 2.0.21 | Apache 2.0 | [JetBrains/kotlin](https://github.com/JetBrains/kotlin) |
| KSP (Kotlin Symbol Processing) | 2.0.21 | Apache 2.0 | [google/ksp](https://github.com/google/ksp) |
| Gradle | 8.x | Apache 2.0 | [gradle/gradle](https://github.com/gradle/gradle) |

## Android Framework

| Library | Version | License | Source |
|---------|---------|---------|--------|
| androidx.core:core-ktx | 1.13.1 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| androidx.core:core-splashscreen | 1.0.1 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |

## UI (Jetpack Compose)

| Library | Version | License | Source |
|---------|---------|---------|--------|
| androidx.compose.ui:ui | 2024.10.01 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| androidx.compose.material3:material3 | 2024.10.01 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| androidx.activity:activity-compose | 1.9.3 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| androidx.lifecycle:lifecycle-runtime-compose | 2.8.6 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| androidx.navigation:navigation-compose | 2.8.3 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |

## Dependency Injection

| Library | Version | License | Source |
|---------|---------|---------|--------|
| com.google.dagger:hilt-android | 2.52 | Apache 2.0 | [google/dagger](https://github.com/google/dagger) |
| androidx.hilt:hilt-navigation-compose | 1.2.0 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |

## Database

| Library | Version | License | Source |
|---------|---------|---------|--------|
| androidx.room:room-runtime | 2.6.1 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| androidx.room:room-ktx | 2.6.1 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |

## Cryptography & Security

| Library | Version | License | Source |
|---------|---------|---------|--------|
| androidx.security:security-crypto | 1.1.0-alpha06 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |
| **com.google.crypto.tink:tink-android** | **1.10.0** | **Apache 2.0** | **[google/tink](https://github.com/google/tink)** |

### Tink Details

**Google Tink** is Google's cryptography library used by:
- Android OS (internally)
- Google Cloud
- Many security-critical applications

**Why Tink?**
- Audited by security researchers
- Protects against common crypto mistakes
- Provides AEAD (Authenticated Encryption with Associated Data)
- Actively maintained

**How Cipher uses it:**
- Tink AEAD interface wraps AES-256-GCM
- Key derivation: PBKDF2 (200k iterations, SHA-256)
- No reliance on Tink's key management system (Cipher derives keys from passwords)

## Serialization

| Library | Version | License | Source |
|---------|---------|---------|--------|
| **org.jetbrains.kotlinx:kotlinx-serialization-json** | **1.6.3** | **Apache 2.0** | **[JetBrains/kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)** |

### Serialization Details

**kotlinx-serialization** is JetBrains' official Kotlin serialization.

**Why not Gson/Moshi?**
- Gson has had crypto vulnerabilities before
- Moshi has fewer audits
- kotlinx-serialization is Kotlin-first and lightweight
- Same maintainers as Kotlin itself

**How Cipher uses it:**
- Serialize TodoItem lists to JSON for storage
- No untrusted deserialization (app-controlled JSON only)

## Logging (Optional)

| Library | Version | License | Source |
|---------|---------|---------|--------|
| **com.jakewharton.timber:timber** | **5.0.1** | **Apache 2.0** | **[JakeWharton/timber](https://github.com/JakeWharton/timber)** |

### Timber Details

**Timber** is a logging utility by Jake Wharton.

**Why Timber?**
- Lightweight (no performance overhead)
- Can be disabled in release builds
- Clean API

**How Cipher uses it:**
- Optional. Can be removed without affecting functionality.
- Debugging logs only (not sent anywhere)

## Async / Coroutines

| Library | Version | License | Source |
|---------|---------|---------|--------|
| org.jetbrains.kotlinx:kotlinx-coroutines-android | 1.9.0 | Apache 2.0 | [JetBrains/kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines) |

## Lifecycle Management

| Library | Version | License | Source |
|---------|---------|---------|--------|
| androidx.lifecycle:lifecycle-viewmodel-compose | 2.8.6 | Apache 2.0 | [android/androidx](https://android.googlesource.com/platform/frameworks/support) |

## Total Dependency Count

- **Direct dependencies:** 15
- **Transitive:** ~50+
- **All FOSS, all auditable**

No hidden APIs. No telemetry hooks. All code reviewable.

---

**Last updated:** 2026
**Cipher v1.0.0**
