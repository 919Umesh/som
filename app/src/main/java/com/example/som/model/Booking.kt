package com.example.som.model

data class Booking(
    val id: String,
    val bookingNumber: String,
    val serviceId: String,
    val serviceName: String,
    val provider: String,
    val scheduledDate: String,
    val scheduledTime: String,
    val customerName: String,
    val contact: String,
    val address: String,
    val status: BookingStatus
)

enum class BookingStatus {
    CONFIRMED,
    CANCELLED
}

data class CreateBookingRequest(
    val serviceId: String,
    val date: String,
    val timeSlotId: String,
    val customerName: String,
    val contact: String,
    val address: String
)
