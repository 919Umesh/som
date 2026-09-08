package com.example.som.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.som.ui.components.ServiceCard
import com.example.som.viewmodel.ServiceListState
import com.example.som.viewmodel.ServiceListViewModel

@Composable
fun ServiceListScreen(
    viewModel: ServiceListViewModel,
    onServiceClick: (String) -> Unit,
    onMyBookingsClick: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.searchQuery.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 36.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Services", style = MaterialTheme.typography.headlineMedium)
            Button(onClick = onMyBookingsClick) { Text("My bookings") }
        }
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::onSearchQueryChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search services") },
            singleLine = true
        )
        when (val currentState = state) {
            ServiceListState.Loading -> ScreenMessage { CircularProgressIndicator() }
            ServiceListState.Empty -> ScreenMessage {
                Text(if (query.isBlank()) "No services available" else "No services match your search")
            }
            is ServiceListState.Error -> ScreenMessage {
                Text(currentState.message)
                Button(onClick = { viewModel.loadServices() }) { Text("Retry") }
            }
            is ServiceListState.Success -> LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(currentState.services, key = { it.id }) { service ->
                    ServiceCard(service = service, onClick = { onServiceClick(service.id) })
                }
            }
        }
    }
}

@Composable
fun ScreenMessage(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) { content() }
    }
}
