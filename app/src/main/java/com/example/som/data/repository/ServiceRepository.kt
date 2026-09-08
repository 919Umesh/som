package com.example.som.data.repository

import com.example.som.data.api.SomApiService
import com.example.som.model.CreateBookingRequest

class ServiceRepository(
    private val api: SomApiService
) {
    suspend fun getServices(query: String? = null) = api.getServices(query)
    suspend fun getServiceById(id: String) = api.getServiceById(id)
    suspend fun getAvailability(serviceId: String, date: String) = api.getAvailability(serviceId, date)
    suspend fun createBooking(request: CreateBookingRequest) = api.createBooking(request)
    suspend fun getBookings() = api.getBookings()
}
