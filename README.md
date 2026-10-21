# Meghlume

Safari-inspired Android browser built with Kotlin, Jetpack Compose and GeckoView
(Firefox's Gecko engine, no Chromium).

## Features
- Floating bottom address pill that collapses on scroll
- Tab switcher (grid), private tabs
- Tracker, fingerprinting and cookie protection (GeckoView content blocking)
- QR code scanner (camera permission requested at runtime)
- Downloads via Android DownloadManager
- Can be set as default browser (opens http/https links)

## Get an APK without a PC
1. Upload this folder to a new GitHub repository.
2. Open the **Actions** tab, run **Build APK** (or push to `main`).
3. Download `Meghlume-debug-apk` from the finished run and install it.

## Build in Android Studio
Open the folder, let Gradle sync, press Run. Requires JDK 17 and Android SDK 35.

## GeckoView version
`gradle.properties` uses `geckoviewVersion=+` (latest stable). After the first
successful build, pin it to the exact version Gradle resolved.
