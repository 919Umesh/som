# API Contract

Base path: `/api/v1`  
Media type: `application/json`

All error responses use this conceptual envelope:

```json
{
  "error": {
    "code": "validation_error",
    "message": "Please correct the highlighted fields",
    "field_errors": { "customerName": "Name is required" }
  }
}
```

`field_errors` is present only for field validation. In the app, responses map to `ApiResult.Success<T>` or `ApiResult.Failure(ApiError)`.

## List/search services

`GET /api/v1/services`

- Query: `query` (optional string), matched against service name, category, and provider.
- `200`: array of service objects. An empty array is a valid response and renders the empty state.
- `500`: generic server error; the UI shows a retry action.

Service response fields: `id`, `name`, `category`, `provider`, `price`, `currency`, `durationMinutes`, `rating`, and `description`.

## Service details

`GET /api/v1/services/{service_id}`

- Path: `service_id` (required string).
- `200`: one service object.
- `404`: `not_found` error.
- `500`: generic server error.

The UI shows a loading state and a recoverable retry screen.

## Availability

`GET /api/v1/services/{service_id}/availability?date={date}`

- Path: `service_id` (required string).
- Query: `date` (required ISO-8601 calendar date, `yyyy-MM-dd`).
- `200`: array of `{ "id", "displayTime", "isAvailable" }`.
- `400`: validation error for a missing/invalid date.
- `404`: service not found.
- `500`: generic server error.

An empty array or an array with no available entries displays a no-slots message. A failed request displays an inline retry action.

## Create booking

`POST /api/v1/bookings`

Request body:

```json
{
  "serviceId": "home-cleaning",
  "date": "2026-09-09",
  "timeSlotId": "home-cleaning|2026-09-09|09:00",
  "customerName": "Aarav Sharma",
  "contact": "9800000000",
  "address": "Kathmandu"
}
```

- `201`: booking with `id`, `bookingNumber`, service/provider snapshot, scheduled date/time, customer fields, and status.
- `400` or `422`: structured validation error with `field_errors`.
- `404`: service not found.
- `409`: `slot_conflict` when another customer reserved the chosen slot.
- `500`: generic server error.

The confirmation button is disabled while loading. Validation returns the user to editable fields; conflict offers a route back to refreshed availability; generic errors allow retry.

## List customer bookings

`GET /api/v1/bookings`

- In a real authenticated API, customer identity comes from the session/token rather than a client-controlled query.
- `200`: array of bookings, newest first. An empty array renders an empty state.
- `401`: unauthenticated (documented for the future backend; authentication is out of scope here).
- `500`: generic server error with retry.
