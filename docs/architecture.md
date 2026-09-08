# Architecture

## Overview

The app follows a deliberately small layered design:

```text
Compose screens → ViewModels → ServiceRepository → SomApiService → MockSomApiService
```

- `ui/screens` renders immutable state and forwards user actions. It does not call the API or contain mock data.
- `ui/components` contains reusable presentation components such as `ServiceCard`.
- `viewmodel` owns asynchronous work, form validation, selection, and UI state transitions.
- `data/repository` is the application-facing data boundary. A future cache or mapping layer can be added here without changing screens.
- `data/api` defines operations and structured result/error types.
- `data/mock` simulates an HTTP implementation, latency, mutable booking storage, and failure responses.
- `model` contains service, availability, request, and booking models.
- `navigation` coordinates the single activity's screen flow and supplies ViewModels.

## State management

Each screen observes a read-only `StateFlow` using `collectAsStateWithLifecycle`, so collection stops when the UI lifecycle is not active. ViewModels launch work in `viewModelScope` and rethrow `CancellationException` rather than presenting cancellation as an error.

List, details, availability, and bookings explicitly represent loading, content/empty, and recoverable error states. Booking uses a `FORM → REVIEW → CONFIRMED` state machine. Submission is disabled while a request is active, preventing duplicate bookings.

Search work is debounced and the previous job is cancelled when the query changes, preventing an older response from replacing a newer query result.

## API boundary and replacement

`SomApiService` is independent of Compose and Android UI types. `MockSomApiService` is supplied only at the composition root in `MainActivity`. Replacing it with a Retrofit implementation requires implementing the same five suspend operations and changing that single construction point. The repository and presentation layers remain unchanged.

The mock stores bookings for the lifetime of the activity/process. Persistent offline storage is intentionally excluded from this assignment's scope.
