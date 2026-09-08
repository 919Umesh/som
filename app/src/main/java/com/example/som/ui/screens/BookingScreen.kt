package com.example.som.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.som.ui.components.BackButton
import com.example.som.viewmodel.BookingStep
import com.example.som.viewmodel.BookingUiState
import com.example.som.viewmodel.BookingViewModel

@Composable
fun BookingScreen(
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onChooseDifferentTime: () -> Unit,
    onViewBookings: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        when (state.step) {
            BookingStep.FORM -> BookingForm(state, viewModel, onBack)
            BookingStep.REVIEW -> BookingReview(state, viewModel, onBack, onChooseDifferentTime)
            BookingStep.CONFIRMED -> BookingConfirmation(state, onViewBookings)
        }
    }
}

@Composable
private fun BookingForm(state: BookingUiState, viewModel: BookingViewModel, onBack: () -> Unit) {
    BackButton(onClick = onBack)
    Text("Your details", style = MaterialTheme.typography.headlineMedium)
    BookingSchedule(state)
    OutlinedTextField(
        value = state.customerName,
        onValueChange = viewModel::onNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Customer name") },
        shape = RoundedCornerShape(24.dp),
        isError = "customerName" in state.fieldErrors,
        supportingText = { state.fieldErrors["customerName"]?.let { Text(it) } }
    )
    OutlinedTextField(
        value = state.contact,
        onValueChange = viewModel::onContactChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Phone or email") },
        shape = RoundedCornerShape(24.dp),
        isError = "contact" in state.fieldErrors,
        supportingText = { state.fieldErrors["contact"]?.let { Text(it) } }
    )
    OutlinedTextField(
        value = state.address,
        onValueChange = viewModel::onAddressChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Service address") },
        shape = RoundedCornerShape(24.dp),
        isError = "address" in state.fieldErrors,
        supportingText = { state.fieldErrors["address"]?.let { Text(it) } }
    )
    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    Button(onClick = viewModel::reviewBooking, modifier = Modifier.fillMaxWidth()) { Text("Review booking") }
}

@Composable
private fun BookingReview(
    state: BookingUiState,
    viewModel: BookingViewModel,
    onBack: () -> Unit,
    onChooseDifferentTime: () -> Unit
) {
    BackButton(onClick = onBack, enabled = !state.isSubmitting)
    Text("Review booking", style = MaterialTheme.typography.headlineMedium)
    BookingSchedule(state)
    Text("Name: ${state.customerName}")
    Text("Contact: ${state.contact}")
    Text("Address: ${state.address}")
    state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    if (state.isSlotConflict) {
        Button(onClick = onChooseDifferentTime, modifier = Modifier.fillMaxWidth()) { Text("Choose another time") }
    } else {
        Button(
            onClick = viewModel::confirmBooking,
            enabled = !state.isSubmitting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSubmitting) CircularProgressIndicator() else Text("Confirm booking")
        }
    }
    OutlinedButton(
        onClick = viewModel::editDetails,
        enabled = !state.isSubmitting,
        modifier = Modifier.fillMaxWidth()
    ) { Text("Edit customer details") }
}

@Composable
private fun BookingConfirmation(state: BookingUiState, onViewBookings: () -> Unit) {
    val booking = state.booking ?: return
    Text("Booking confirmed", style = MaterialTheme.typography.headlineMedium)
    Text("Booking number: ${booking.bookingNumber}")
    BookingSchedule(state)
    Text("Status: ${booking.status.name.lowercase().replaceFirstChar { it.uppercase() }}")
    Button(onClick = onViewBookings, modifier = Modifier.fillMaxWidth()) { Text("View my bookings") }
}

@Composable
private fun BookingSchedule(state: BookingUiState) {
    Text(state.draft.service.name, style = MaterialTheme.typography.titleMedium)
    Text("${state.draft.service.provider} • ${state.draft.date} at ${state.draft.timeSlot.displayTime}")
    Text("${state.draft.service.currency} ${state.draft.service.price.toInt()}")
}
