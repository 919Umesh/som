package com.example.som.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.som.data.api.ApiResult
import com.example.som.data.repository.ServiceRepository
import com.example.som.model.Booking
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MyBookingsState {
    data object Loading : MyBookingsState
    data object Empty : MyBookingsState
    data class Success(val bookings: List<Booking>) : MyBookingsState
    data class Error(val message: String) : MyBookingsState
}

class MyBookingsViewModel(
    private val repository: ServiceRepository
) : ViewModel() {
    private val _state = MutableStateFlow<MyBookingsState>(MyBookingsState.Loading)
    val state = _state.asStateFlow()

    fun loadBookings() {
        viewModelScope.launch {
            _state.value = MyBookingsState.Loading
            try {
                when (val result = repository.getBookings()) {
                    is ApiResult.Success -> _state.value = if (result.data.isEmpty()) {
                        MyBookingsState.Empty
                    } else {
                        MyBookingsState.Success(result.data)
                    }
                    is ApiResult.Failure -> _state.value = MyBookingsState.Error(result.error.userMessage)
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = MyBookingsState.Error("Unable to load bookings")
            }
        }
    }
}
