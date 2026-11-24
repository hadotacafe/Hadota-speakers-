# Hadota Speakers

Native Android app for Hadota Cafe.

## Build
GitHub Actions builds a debug APK on every push to `main`.
Open the Actions tab and download the `Hadota-Speakers-debug-apk` artifact.

## Current capabilities
- Shows paired Bluetooth devices.
- Opens Android Bluetooth settings for pairing and connection.
- Controls Android media-stream volume.

## Android limitation
Third-party apps generally cannot independently set the hardware volume of each Bluetooth speaker. The app does not claim to provide independent per-speaker volume control or a system-wide equalizer.
