# ForestNavigator

Offline-first forest navigation for Android.

## Features

- Map view with OpenStreetMap tiles and local tile caching
- Save named GPS waypoints
- Record GPS tracks in the background
- Navigate to a saved waypoint with distance/bearing
- Voice guidance using Android Text-to-Speech
- GPX export for waypoints and recorded tracks
- Material 3 interface

## Build

The project is built by GitHub Actions. A debug APK can be built locally with `gradle assembleDebug` after installing the Android SDK.

## Release signing

The release workflow supports a keystore supplied through GitHub Actions secrets. **Never commit the keystore or passwords to the repository.** See `.github/workflows/release.yml`.

## Safety

GPS navigation is an aid, not a substitute for a paper map, compass, weather awareness, or local safety procedures. ForestNavigator does not guarantee positional accuracy or route safety.
