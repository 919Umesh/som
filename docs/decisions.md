# Technical Decisions

## 1. Structured results instead of unchecked API exceptions

**Choice:** API operations return `ApiResult<T>` with typed `ApiError` variants.

**Why:** Validation, conflict, not-found, and server errors require different UI recovery. Making them explicit prevents screens from parsing exception text and makes the mock contract close to HTTP semantics.

**Alternative:** Throw Retrofit/HTTP exceptions from the API layer. This was not selected because it couples presentation behavior to transport details and makes expected business failures look exceptional.

## 2. In-memory mock behind the production-shaped interface

**Choice:** `MockSomApiService` implements `SomApiService`, simulates latency, and owns temporary bookings and reserved slots.

**Why:** It supports the complete flow without a backend while keeping the replacement seam obvious. Shared in-memory state means a newly created booking appears immediately in My Bookings.

**Alternative:** Hardcoded lists in Compose or direct JSON asset reads. Those approaches were rejected because they bypass the API/repository boundary and make error/latency behavior difficult to model.

## 3. Explicit presentation states and a booking state machine

**Choice:** Read-only `StateFlow` values expose sealed loading/content/empty/error states; booking uses form, review, and confirmed steps.

**Why:** Every visible result is deterministic and testable. The review step meets the requirement that users see a summary before the mutation, while `isSubmitting` prevents duplicate actions.

**Alternative:** Several unrelated booleans such as `isLoading`, `hasError`, and `isConfirmed`. That allows impossible combinations and obscures transitions.

## 4. Lightweight navigation coordinator

**Choice:** A small Compose coordinator models the four destinations and scopes ViewModels with factories.

**Why:** The assignment has one linear flow and does not require deep links or multiple back stacks. This keeps dependencies and setup proportional to the app while retaining screen isolation.

**Alternative:** Navigation Compose. It is the better choice once deep links, process-restored arguments, nested graphs, or a larger destination set are required, but those costs do not add value to this vertical slice.

## 5. A deterministic conflict slot

**Choice:** The mock returns `10:30` as initially available, then rejects it at creation time.

**Why:** Slot conflicts are races: availability can change between read and write. A deterministic slot makes this important behavior easy to demonstrate and test without random failures.

**Alternative:** Randomly reject bookings. Random behavior was rejected because it makes demos and automated tests flaky.
