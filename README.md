# SOM Customer Service Booking

A small native Android/Jetpack Compose app implementing the customer flow:

`Service List → Service Details → Date & Time → Booking Review/Confirmation → My Bookings`

The app uses ViewModels, coroutines, repositories, an API-style contract, and an in-memory mock API. No backend is required. The mock adds latency and supports success, empty, server-error, validation-error, and slot-conflict responses.

## Quick start

Open the project in Android Studio with JDK 17, sync Gradle, and run the `app` configuration on an Android 7.0 (API 24) or newer emulator/device.

```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

The generated APK is at `app/build/outputs/apk/debug/app-debug.apk`.

## Demo notes

- Search by service, category, or provider on the service list.
- Choose any available date and time to complete a booking.
- Selecting `10:30` deliberately simulates a real-world race and returns a `409`-style slot conflict. Choose another time to continue.
- Empty and generic server responses are exposed through `MockFailureMode` and covered by tests.

A screen-recorded demo is not included in this repository.

## Documentation

- [Architecture](docs/architecture.md)
- [API contract](docs/api-contract.md)
- [Technical decisions](docs/decisions.md)
- [Setup, testing, and APK generation](docs/setup.md)
