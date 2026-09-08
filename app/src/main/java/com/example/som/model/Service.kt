package com.example.som.model

data class Service(
    val id: String,
    val name: String,
    val category: String,
    val provider: String,
    val price: Double,
    val currency: String,
    val durationMinutes: Int,
    val rating: Double,
    val description: String
)
