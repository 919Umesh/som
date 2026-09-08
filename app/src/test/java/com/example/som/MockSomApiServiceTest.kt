package com.example.som

import com.example.som.data.api.ApiError
import com.example.som.data.api.ApiResult
import com.example.som.data.mock.MockFailureMode
import com.example.som.data.mock.MockSomApiService
import com.example.som.model.CreateBookingRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockSomApiServiceTest {
    @Test
    fun serviceListReturnsSuccessAndSupportsSearch() = runBlocking {
        val api = MockSomApiService(latencyMillis = 0)

        val result = api.getServices("plumb")

        assertTrue(result is ApiResult.Success)
        assertEquals("Plumbing Repair", (result as ApiResult.Success).data.single().name)
    }

    @Test
    fun serviceListSupportsEmptyAndServerErrorScenarios() = runBlocking {
        val api = MockSomApiService(latencyMillis = 0, failureMode = MockFailureMode.EMPTY_SERVICES)
        val empty = api.getServices()
        assertTrue(empty is ApiResult.Success && empty.data.isEmpty())

        api.failureMode = MockFailureMode.SERVER_ERROR
        val error = api.getServices()
        assertTrue(error is ApiResult.Failure && error.error is ApiError.Server)
    }

    @Test
    fun serviceDetailsAndAvailabilityAreReturned() = runBlocking {
        val api = MockSomApiService(latencyMillis = 0)

        val details = api.getServiceById("home-cleaning")
        val availability = api.getAvailability("home-cleaning", "2026-09-09")

        assertTrue(details is ApiResult.Success)
        assertEquals("Home Cleaning", (details as ApiResult.Success).data.name)
        assertTrue(availability is ApiResult.Success && availability.data.isNotEmpty())
    }

    @Test
    fun validBookingIsCreatedAndAppearsInBookings() = runBlocking {
        val api = MockSomApiService(latencyMillis = 0)
        val request = validRequest(slot = "home-cleaning|2026-09-09|09:00")

        val created = api.createBooking(request)
        val bookings = api.getBookings()

        assertTrue(created is ApiResult.Success)
        assertTrue(bookings is ApiResult.Success)
        assertEquals((created as ApiResult.Success).data, (bookings as ApiResult.Success).data.single())
    }

    @Test
    fun bookingReturnsValidationAndSlotConflictErrors() = runBlocking {
        val api = MockSomApiService(latencyMillis = 0)
        val invalid = api.createBooking(validRequest(slot = "").copy(customerName = ""))
        val conflict = api.createBooking(validRequest(slot = "home-cleaning|2026-09-09|10:30"))

        assertTrue(invalid is ApiResult.Failure && invalid.error is ApiError.Validation)
        assertTrue(conflict is ApiResult.Failure && conflict.error is ApiError.Conflict)
    }

    private fun validRequest(slot: String) = CreateBookingRequest(
        serviceId = "home-cleaning",
        date = "2026-09-09",
        timeSlotId = slot,
        customerName = "Aarav Sharma",
        contact = "9800000000",
        address = "Kathmandu"
    )
}
