package com.example.som.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.som.model.BookingDraft
import com.example.som.ui.components.BackButton
import com.example.som.viewmodel.ServiceDetailsState
import com.example.som.viewmodel.ServiceDetailsViewModel

@Composable
fun ServiceDetailsScreen(
    viewModel: ServiceDetailsViewModel,
    onBack: () -> Unit,
    onContinue: (BookingDraft) -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when (val current = state) {
        ServiceDetailsState.Loading -> ScreenMessage { CircularProgressIndicator() }
        is ServiceDetailsState.Error -> ScreenMessage {
            Text(current.message)
            Button(onClick = viewModel::loadService) { Text("Retry") }
            BackButton(onClick = onBack)
        }
        is ServiceDetailsState.Success -> Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BackButton(onClick = onBack)
            Text(current.service.name, style = MaterialTheme.typography.headlineMedium)
            Text(current.service.description)
            Text("Provider: ${current.service.provider}")
            Text("${current.service.currency} ${current.service.price.toInt()} • ${current.service.durationMinutes} min • ★ ${current.service.rating}")
            Text("Select a date", style = MaterialTheme.typography.titleMedium)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(current.availableDates) { date ->
                    FilterChip(
                        selected = current.selectedDate == date,
                        onClick = { viewModel.selectDate(date) },
                        label = { Text(date) },
                        colors = somFilterChipColors()
                    )
                }
            }
            Text("Available times", style = MaterialTheme.typography.titleMedium)
            when {
                current.isLoadingSlots -> CircularProgressIndicator()
                current.slotError != null -> Column {
                    Text(current.slotError)
                    Button(onClick = viewModel::refreshAvailability) { Text("Retry") }
                }
                current.slots.none { it.isAvailable } -> Text("No time slots available for this date")
                else -> LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(current.slots, key = { it.id }) { slot ->
                        FilterChip(
                            selected = current.selectedSlot?.id == slot.id,
                            onClick = { viewModel.selectSlot(slot) },
                            enabled = slot.isAvailable,
                            label = { Text(slot.displayTime) },
                            colors = somFilterChipColors()
                        )
                    }
                }
            }
            Button(
                onClick = {
                    current.selectedSlot?.let { slot ->
                        onContinue(BookingDraft(current.service, current.selectedDate, slot))
                    }
                },
                enabled = current.selectedSlot != null && !current.isLoadingSlots,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Continue to booking") }
        }
    }
}

@Composable
private fun somFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = MaterialTheme.colorScheme.surfaceContainer,
    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
)
