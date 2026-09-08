package com.example.som.model

data class BookingDraft(
    val service: Service,
    val date: String,
    val timeSlot: TimeSlot
)
