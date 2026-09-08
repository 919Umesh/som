package com.example.som.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.som.ui.components.BackButton
import com.example.som.viewmodel.MyBookingsState
import com.example.som.viewmodel.MyBookingsViewModel

@Composable
fun MyBookingsScreen(viewModel: MyBookingsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadBookings() }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BackButton(onClick = onBack)
        Text("My bookings", style = MaterialTheme.typography.headlineMedium)
        when (val current = state) {
            MyBookingsState.Loading -> ScreenMessage { CircularProgressIndicator() }
            MyBookingsState.Empty -> ScreenMessage { Text("You have no bookings yet") }
            is MyBookingsState.Error -> ScreenMessage {
                Text(current.message)
                Button(onClick = viewModel::loadBookings) { Text("Retry") }
            }
            is MyBookingsState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(current.bookings, key = { it.id }) { booking ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(booking.serviceName, style = MaterialTheme.typography.titleMedium)
                            Text("Booking: ${booking.bookingNumber}")
                            Text(booking.provider)
                            Text("${booking.scheduledDate} at ${booking.scheduledTime}")
                            Text("Status: ${booking.status.name.lowercase().replaceFirstChar { it.uppercase() }}")
                        }
                    }
                }
            }
        }
    }
}
