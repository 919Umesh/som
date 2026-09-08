package com.example.som.data.mock

import com.example.som.data.api.ApiError
import com.example.som.data.api.ApiResult
import com.example.som.data.api.SomApiService
import com.example.som.model.Booking
import com.example.som.model.BookingStatus
import com.example.som.model.CreateBookingRequest
import com.example.som.model.Service
import com.example.som.model.TimeSlot
import kotlinx.coroutines.delay
import java.util.UUID

enum class MockFailureMode {
    NONE,
    EMPTY_SERVICES,
    SERVER_ERROR
}

class MockSomApiService(
    private val latencyMillis: Long = 650,
    var failureMode: MockFailureMode = MockFailureMode.NONE
) : SomApiService {
    private val bookings = mutableListOf<Booking>()
    private val reservedSlots = mutableSetOf<String>()

    override suspend fun getServices(query: String?): ApiResult<List<Service>> {
        simulateLatency()
        failureModeResult<List<Service>>()?.let { return it }
        if (failureMode == MockFailureMode.EMPTY_SERVICES) return ApiResult.Success(emptyList())

        val normalizedQuery = query.orEmpty().trim()
        return ApiResult.Success(
            services.filter { service ->
                normalizedQuery.isBlank() || listOf(
                    service.name,
                    service.category,
                    service.provider
                ).any { it.contains(normalizedQuery, ignoreCase = true) }
            }
        )
    }

    override suspend fun getServiceById(id: String): ApiResult<Service> {
        simulateLatency()
        failureModeResult<Service>()?.let { return it }
        return services.firstOrNull { it.id == id }
            ?.let { ApiResult.Success(it) }
            ?: ApiResult.Failure(ApiError.NotFound())
    }

    override suspend fun getAvailability(
        serviceId: String,
        date: String
    ): ApiResult<List<TimeSlot>> {
        simulateLatency()
        failureModeResult<List<TimeSlot>>()?.let { return it }
        if (services.none { it.id == serviceId }) return ApiResult.Failure(ApiError.NotFound())
        if (date.isBlank()) {
            return ApiResult.Failure(
                ApiError.Validation(mapOf("date" to "Date is required"))
            )
        }

        return ApiResult.Success(
            listOf("09:00", "10:30", "13:00", "15:30").mapIndexed { index, time ->
                val id = "$serviceId|$date|$time"
                TimeSlot(
                    id = id,
                    displayTime = time,
                    isAvailable = id !in reservedSlots && index != 3
                )
            }
        )
    }

    override suspend fun createBooking(request: CreateBookingRequest): ApiResult<Booking> {
        simulateLatency()
        failureModeResult<Booking>()?.let { return it }

        val fieldErrors = buildMap {
            if (request.customerName.isBlank()) put("customerName", "Name is required")
            if (request.contact.isBlank()) put("contact", "Contact is required")
            if (request.address.isBlank()) put("address", "Address is required")
            if (request.date.isBlank()) put("date", "Date is required")
            if (request.timeSlotId.isBlank()) put("timeSlotId", "Time slot is required")
        }
        if (fieldErrors.isNotEmpty()) {
            return ApiResult.Failure(ApiError.Validation(fieldErrors))
        }

        val service = services.firstOrNull { it.id == request.serviceId }
            ?: return ApiResult.Failure(ApiError.NotFound())
        val slotKey = request.timeSlotId

        // 10:30 models another customer reserving a slot between availability and checkout.
        if (slotKey in reservedSlots || slotKey.endsWith("|10:30")) {
            reservedSlots += slotKey
            return ApiResult.Failure(ApiError.Conflict())
        }

        reservedSlots += slotKey
        val booking = Booking(
            id = UUID.randomUUID().toString(),
            bookingNumber = "SOM-${(bookings.size + 1).toString().padStart(4, '0')}",
            serviceId = service.id,
            serviceName = service.name,
            provider = service.provider,
            scheduledDate = request.date,
            scheduledTime = slotKey.substringAfterLast('|'),
            customerName = request.customerName.trim(),
            contact = request.contact.trim(),
            address = request.address.trim(),
            status = BookingStatus.CONFIRMED
        )
        bookings.add(0, booking)
        return ApiResult.Success(booking)
    }

    override suspend fun getBookings(): ApiResult<List<Booking>> {
        simulateLatency()
        failureModeResult<List<Booking>>()?.let { return it }
        return ApiResult.Success(bookings.toList())
    }

    private suspend fun simulateLatency() {
        if (latencyMillis > 0) delay(latencyMillis)
    }

    private fun <T> failureModeResult(): ApiResult<T>? = when (failureMode) {
        MockFailureMode.SERVER_ERROR -> ApiResult.Failure(ApiError.Server())
        MockFailureMode.NONE,
        MockFailureMode.EMPTY_SERVICES -> null
    }

    private companion object {
        val services = listOf(
            Service(
                id = "home-cleaning",
                name = "Home Cleaning",
                category = "Cleaning",
                provider = "SOM Cleaning",
                price = 1500.0,
                currency = "NPR",
                durationMinutes = 120,
                rating = 4.8,
                description = "A thorough home cleaning service covering living spaces, kitchen, and bathrooms."
            ),
            Service(
                id = "laundry",
                name = "Laundry & Ironing",
                category = "Laundry",
                provider = "Fresh Fold",
                price = 900.0,
                currency = "NPR",
                durationMinutes = 90,
                rating = 4.6,
                description = "Convenient washing and ironing for everyday clothing, handled with care."
            ),
            Service(
                id = "plumbing",
                name = "Plumbing Repair",
                category = "Maintenance",
                provider = "SOM Home Care",
                price = 1200.0,
                currency = "NPR",
                durationMinutes = 60,
                rating = 4.7,
                description = "Inspection and repair for common leaks, blocked drains, and faulty fixtures."
            )
        )
    }
}
