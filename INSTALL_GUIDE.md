# AroPi Installation Guide

## Why Does the App Disappear?

When you run the app from Android Studio using the "Run" button, it installs a **debug build** that may be removed by the system or Android Studio's instant run feature. To keep the app permanently installed, you need to install a **release build**.

## Building Release APK

### 1. Build the APK
```bash
./gradlew assembleRelease
```

The APK will be created at:
```
app/build/outputs/apk/release/app-release-unsigned.apk
```

### 2. Install on Phone

#### Option A: Via ADB (if phone is connected)
```bash
adb install app/build/outputs/apk/release/app-release-unsigned.apk
```

#### Option B: Manual Installation (Recommended)
1. Copy `app-release-unsigned.apk` to your phone
2. Open the file on your phone
3. Allow installation from unknown sources if prompted
4. Tap "Install"

The app will now **stay permanently** on your phone.

## For Distribution (Optional)

To create a signed APK for distribution:

### 1. Create a Keystore (one-time setup)
```bash
keytool -genkey -v -keystore aropi-release-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias aropi
```

### 2. Add to `app/build.gradle.kts`
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../aropi-release-key.jks")
            storePassword = "your-password"
            keyAlias = "aropi"
            keyPassword = "your-password"
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
```

### 3. Build Signed APK
```bash
./gradlew assembleRelease
```

The signed APK will be at:
```
app/build/outputs/apk/release/app-release.apk
```

## Quick Commands

```bash
# Build release APK
./gradlew assembleRelease

# Install on connected device
adb install -r app/build/outputs/apk/release/app-release-unsigned.apk

# Build and install in one command
./gradlew installRelease
```

## Troubleshooting

**"App not installed"**: Enable "Install from unknown sources" in phone settings

**"Signature conflict"**: Uninstall the old debug version first
```bash
adb uninstall com.aropi.app
```

**Can't find APK**: Make sure the build completed successfully
```bash
ls -la app/build/outputs/apk/release/
```
