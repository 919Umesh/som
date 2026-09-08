# Setup and Build

## Prerequisites

- Android Studio with Android SDK 37 installed
- JDK 17
- An emulator or physical device running Android 7.0 / API 24 or newer

## Run from Android Studio

1. Open the repository root in Android Studio.
2. Allow Gradle sync to finish.
3. Select the `app` run configuration and a device.
4. Click **Run**.

No API keys, backend, local properties beyond the normal Android SDK path, or account setup are required.

## Command-line verification

On macOS/Linux:

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

On Windows:

```powershell
gradlew.bat testDebugUnitTest
gradlew.bat assembleDebug
```

Unit tests cover list success/search, empty and server-error scenarios, details/availability retrieval, form validation, successful booking/listing, slot conflict, and an initial ViewModel state transition.

## APK

Generate a debug APK with:

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

For a distributable release APK, add a private signing configuration outside source control and run `./gradlew assembleRelease`. The assignment build intentionally does not commit signing credentials.

## Mock scenarios

`MockSomApiService` defaults to successful responses with simulated latency. Construct it with `MockFailureMode.EMPTY_SERVICES` or `MockFailureMode.SERVER_ERROR` to exercise list empty/error behavior. Choose the `10:30` time in the running app to demonstrate a slot conflict. Tests use zero latency for speed.
