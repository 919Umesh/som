package com.example.som.data.api

import com.example.som.model.Booking
import com.example.som.model.CreateBookingRequest
import com.example.som.model.Service
import com.example.som.model.TimeSlot

interface SomApiService {
    suspend fun getServices(query: String? = null): ApiResult<List<Service>>
    suspend fun getServiceById(id: String): ApiResult<Service>
    suspend fun getAvailability(serviceId: String, date: String): ApiResult<List<TimeSlot>>
    suspend fun createBooking(request: CreateBookingRequest): ApiResult<Booking>
    suspend fun getBookings(): ApiResult<List<Booking>>
}
