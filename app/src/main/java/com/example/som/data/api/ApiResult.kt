package com.example.som.data.api

sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}

sealed interface ApiError {
    val userMessage: String

    data class Validation(
        val fieldErrors: Map<String, String>,
        override val userMessage: String = "Please correct the highlighted fields"
    ) : ApiError

    data class Conflict(
        override val userMessage: String = "That time slot is no longer available"
    ) : ApiError

    data class NotFound(
        override val userMessage: String = "The requested item could not be found"
    ) : ApiError

    data class Server(
        override val userMessage: String = "Something went wrong. Please try again"
    ) : ApiError
}
