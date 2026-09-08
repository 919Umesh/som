package com.example.som.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.som.data.api.ApiResult
import com.example.som.data.repository.ServiceRepository
import com.example.som.model.Service
import com.example.som.model.TimeSlot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed interface ServiceDetailsState {
    data object Loading : ServiceDetailsState
    data class Error(val message: String) : ServiceDetailsState
    data class Success(
        val service: Service,
        val availableDates: List<String>,
        val selectedDate: String,
        val slots: List<TimeSlot> = emptyList(),
        val selectedSlot: TimeSlot? = null,
        val isLoadingSlots: Boolean = true,
        val slotError: String? = null
    ) : ServiceDetailsState
}

class ServiceDetailsViewModel(
    private val serviceId: String,
    private val repository: ServiceRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ServiceDetailsState>(ServiceDetailsState.Loading)
    val state = _state.asStateFlow()
    private var availabilityJob: Job? = null

    init {
        loadService()
    }

    fun loadService() {
        viewModelScope.launch {
            _state.value = ServiceDetailsState.Loading
            try {
                when (val result = repository.getServiceById(serviceId)) {
                    is ApiResult.Success -> {
                        val dates = nextDates()
                        _state.value = ServiceDetailsState.Success(
                            service = result.data,
                            availableDates = dates,
                            selectedDate = dates.first()
                        )
                        loadAvailability(dates.first())
                    }
                    is ApiResult.Failure -> _state.value = ServiceDetailsState.Error(result.error.userMessage)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = ServiceDetailsState.Error("Unable to load service details")
            }
        }
    }

    fun selectDate(date: String) {
        val current = _state.value as? ServiceDetailsState.Success ?: return
        if (current.selectedDate == date) return
        _state.value = current.copy(selectedDate = date, selectedSlot = null)
        loadAvailability(date)
    }

    fun selectSlot(slot: TimeSlot) {
        if (!slot.isAvailable) return
        val current = _state.value as? ServiceDetailsState.Success ?: return
        _state.value = current.copy(selectedSlot = slot)
    }

    fun refreshAvailability() {
        val current = _state.value as? ServiceDetailsState.Success ?: return
        loadAvailability(current.selectedDate)
    }

    private fun loadAvailability(date: String) {
        availabilityJob?.cancel()
        val current = _state.value as? ServiceDetailsState.Success ?: return
        _state.value = current.copy(
            selectedDate = date,
            selectedSlot = null,
            isLoadingSlots = true,
            slotError = null
        )
        availabilityJob = viewModelScope.launch {
            try {
                when (val result = repository.getAvailability(serviceId, date)) {
                    is ApiResult.Success -> updateSuccess {
                        it.copy(slots = result.data, isLoadingSlots = false)
                    }
                    is ApiResult.Failure -> updateSuccess {
                        it.copy(isLoadingSlots = false, slotError = result.error.userMessage)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                updateSuccess { it.copy(isLoadingSlots = false, slotError = "Unable to load time slots") }
            }
        }
    }

    private fun updateSuccess(transform: (ServiceDetailsState.Success) -> ServiceDetailsState.Success) {
        val current = _state.value as? ServiceDetailsState.Success ?: return
        _state.value = transform(current)
    }

    private fun nextDates(): List<String> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val calendar = Calendar.getInstance()
        return List(5) {
            val value = formatter.format(calendar.time)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            value
        }
    }
}
