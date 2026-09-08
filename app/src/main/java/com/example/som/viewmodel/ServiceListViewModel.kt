package com.example.som.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.som.data.api.ApiResult
import com.example.som.data.repository.ServiceRepository
import com.example.som.model.Service
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ServiceListState {
    object Loading : ServiceListState()

    data class Success(
        val services: List<Service>
    ) : ServiceListState()

    object Empty : ServiceListState()

    data class Error(
        val message: String
    ) : ServiceListState()
}

class ServiceListViewModel (
    private val repository: ServiceRepository
): ViewModel() {
    private val _state = MutableStateFlow<ServiceListState>(ServiceListState.Loading)
    val state = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadServices()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
        loadServices(debounce = true)
    }

    fun loadServices(debounce: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _state.value = ServiceListState.Loading

            try {
                if (debounce) delay(300)
                when (val result = repository.getServices(_searchQuery.value)) {
                    is ApiResult.Success -> {
                        _state.value = if (result.data.isEmpty()) {
                            ServiceListState.Empty
                        } else {
                            ServiceListState.Success(result.data)
                        }
                    }
                    is ApiResult.Failure -> {
                        _state.value = ServiceListState.Error(result.error.userMessage)
                    }
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                _state.value = ServiceListState.Error("Unable to load services. Please try again")
            }
        }
    }
}
