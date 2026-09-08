package com.example.som.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.som.data.repository.ServiceRepository
import com.example.som.model.BookingDraft
import com.example.som.ui.screens.BookingScreen
import com.example.som.ui.screens.MyBookingsScreen
import com.example.som.ui.screens.ServiceDetailsScreen
import com.example.som.ui.screens.ServiceListScreen
import com.example.som.viewmodel.BookingViewModel
import com.example.som.viewmodel.MyBookingsViewModel
import com.example.som.viewmodel.ServiceDetailsViewModel
import com.example.som.viewmodel.ServiceListViewModel

private sealed interface Destination {
    data object Services : Destination
    data class Details(val serviceId: String, val instance: Int = 0) : Destination
    data class Booking(val draft: BookingDraft, val instance: Int) : Destination
    data object MyBookings : Destination
}

@Composable
fun AppNavigation(repository: ServiceRepository) {
    var destination: Destination by remember { mutableStateOf(Destination.Services) }
    var navigationInstance by remember { mutableIntStateOf(0) }

    BackHandler(enabled = destination != Destination.Services) {
        destination = when (val current = destination) {
            is Destination.Booking -> Destination.Details(current.draft.service.id)
            is Destination.Details,
            Destination.MyBookings,
            Destination.Services -> Destination.Services
        }
    }

    when (val current = destination) {
        Destination.Services -> {
            val viewModel: ServiceListViewModel = viewModel(
                factory = viewModelFactory { ServiceListViewModel(repository) }
            )
            ServiceListScreen(
                viewModel = viewModel,
                onServiceClick = { destination = Destination.Details(it) },
                onMyBookingsClick = { destination = Destination.MyBookings }
            )
        }
        is Destination.Details -> {
            val viewModel: ServiceDetailsViewModel = viewModel(
                key = "details-${current.serviceId}-${current.instance}",
                factory = viewModelFactory { ServiceDetailsViewModel(current.serviceId, repository) }
            )
            ServiceDetailsScreen(
                viewModel = viewModel,
                onBack = { destination = Destination.Services },
                onContinue = { draft ->
                    navigationInstance += 1
                    destination = Destination.Booking(draft, navigationInstance)
                }
            )
        }
        is Destination.Booking -> {
            val viewModel: BookingViewModel = viewModel(
                key = "booking-${current.instance}",
                factory = viewModelFactory { BookingViewModel(current.draft, repository) }
            )
            BookingScreen(
                viewModel = viewModel,
                onBack = { destination = Destination.Details(current.draft.service.id) },
                onChooseDifferentTime = {
                    navigationInstance += 1
                    destination = Destination.Details(current.draft.service.id, navigationInstance)
                },
                onViewBookings = { destination = Destination.MyBookings }
            )
        }
        Destination.MyBookings -> {
            val viewModel: MyBookingsViewModel = viewModel(
                factory = viewModelFactory { MyBookingsViewModel(repository) }
            )
            MyBookingsScreen(viewModel = viewModel, onBack = { destination = Destination.Services })
        }
    }
}

private fun <T : ViewModel> viewModelFactory(create: () -> T): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <VM : ViewModel> create(modelClass: Class<VM>): VM = create() as VM
    }
