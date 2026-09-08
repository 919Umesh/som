package com.example.som.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.som.data.api.ApiError
import com.example.som.data.api.ApiResult
import com.example.som.data.repository.ServiceRepository
import com.example.som.model.Booking
import com.example.som.model.BookingDraft
import com.example.som.model.CreateBookingRequest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BookingStep { FORM, REVIEW, CONFIRMED }

data class BookingUiState(
    val draft: BookingDraft,
    val customerName: String = "",
    val contact: String = "",
    val address: String = "",
    val fieldErrors: Map<String, String> = emptyMap(),
    val step: BookingStep = BookingStep.FORM,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
    val isSlotConflict: Boolean = false,
    val booking: Booking? = null
)

object BookingValidator {
    fun validate(name: String, contact: String, address: String): Map<String, String> = buildMap {
        if (name.isBlank()) put("customerName", "Name is required")
        if (contact.isBlank()) put("contact", "Contact is required")
        if (address.isBlank()) put("address", "Address is required")
    }
}

class BookingViewModel(
    draft: BookingDraft,
    private val repository: ServiceRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BookingUiState(draft = draft))
    val state = _state.asStateFlow()

    fun onNameChange(value: String) = update { it.copy(customerName = value, fieldErrors = it.fieldErrors - "customerName") }
    fun onContactChange(value: String) = update { it.copy(contact = value, fieldErrors = it.fieldErrors - "contact") }
    fun onAddressChange(value: String) = update { it.copy(address = value, fieldErrors = it.fieldErrors - "address") }

    fun reviewBooking() {
        if (_state.value.isSubmitting) return
        val current = _state.value
        val errors = BookingValidator.validate(current.customerName, current.contact, current.address)
        _state.value = current.copy(
            fieldErrors = errors,
            errorMessage = null,
            step = if (errors.isEmpty()) BookingStep.REVIEW else BookingStep.FORM
        )
    }

    fun editDetails() = update { it.copy(step = BookingStep.FORM, errorMessage = null) }

    fun confirmBooking() {
        val current = _state.value
        if (current.isSubmitting || current.step != BookingStep.REVIEW) return
        viewModelScope.launch {
            update { it.copy(isSubmitting = true, errorMessage = null, isSlotConflict = false) }
            val request = CreateBookingRequest(
                serviceId = current.draft.service.id,
                date = current.draft.date,
                timeSlotId = current.draft.timeSlot.id,
                customerName = current.customerName,
                contact = current.contact,
                address = current.address
            )
            try {
                when (val result = repository.createBooking(request)) {
                    is ApiResult.Success -> update {
                        it.copy(isSubmitting = false, step = BookingStep.CONFIRMED, booking = result.data)
                    }
                    is ApiResult.Failure -> handleApiError(result.error)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                update { it.copy(isSubmitting = false, errorMessage = "Unable to create booking. Please try again") }
            }
        }
    }

    private fun handleApiError(error: ApiError) {
        when (error) {
            is ApiError.Validation -> update {
                it.copy(isSubmitting = false, step = BookingStep.FORM, fieldErrors = error.fieldErrors, errorMessage = error.userMessage)
            }
            is ApiError.Conflict -> update {
                it.copy(isSubmitting = false, errorMessage = error.userMessage, isSlotConflict = true)
            }
            else -> update { it.copy(isSubmitting = false, errorMessage = error.userMessage) }
        }
    }

    private fun update(transform: (BookingUiState) -> BookingUiState) {
        _state.value = transform(_state.value)
    }
}
