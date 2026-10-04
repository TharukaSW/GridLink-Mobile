package com.example.gridlink.app.ui.screens.operator

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.remote.MicrogridNodeDto
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.data.repository.StationRepository
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.CyanElectric
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.SolarAmber
import kotlinx.coroutines.launch

@Composable
fun OperatorStationsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val stationRepo = remember { StationRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var stations by remember { mutableStateOf<List<MicrogridNodeDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedStationForSlots by remember { mutableStateOf<MicrogridNodeDto?>(null) }
    var slotsInput by remember { mutableStateOf("") }
    var isUpdatingSlots by remember { mutableStateOf(false) }

    fun loadStations() {
        scope.launch {
            isLoading = true
            when (val res = stationRepo.getAllStations()) {
                is Resource.Success -> {
                    stations = res.data ?: emptyList()
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(res.message ?: "Failed to load solar stations.")
                }
                else -> Unit
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadStations()
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "Station Battery Slots",
                userRole = "GridOperator",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showLogout = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Microgrid Solar Hubs", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("Tap any station to update available battery storage slots", color = Color.Gray, fontSize = 12.sp)
                }
                IconButton(onClick = { loadStations() }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GreenPrimary)
                }
            }

            if (isLoading && stations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (stations.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No microgrid stations registered.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(stations) { station ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedStationForSlots = station
                                    slotsInput = station.availableBatterySlots.toString()
                                },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(station.stationName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        Text("${station.stationCode} • ${station.address}", color = Color.Gray, fontSize = 12.sp)
                                    }
                                    StatusBadge(status = if (station.isActive) "Active" else "Inactive")
                                }

                                Spacer(Modifier.height(10.dp))
                                Divider()
                                Spacer(Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Column {
                                            Text("Available Slots", fontSize = 11.sp, color = Color.Gray)
                                            Text(
                                                "${station.availableBatterySlots} / ${station.totalBatterySlots}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                color = SolarAmber
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Bolt, contentDescription = null, tint = SolarAmber, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Column {
                                            Text("Capacity", fontSize = 11.sp, color = Color.Gray)
                                            Text("${station.capacityKWh} kW/h", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            selectedStationForSlots = station
                                            slotsInput = station.availableBatterySlots.toString()
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Update")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Update Battery Slots Dialog
    if (selectedStationForSlots != null) {
        val station = selectedStationForSlots!!

        AlertDialog(
            onDismissRequest = { selectedStationForSlots = null },
            title = { Text("Update Battery Slots") },
            text = {
                Column {
                    Text("Station: ${station.stationName} (${station.stationCode})", fontWeight = FontWeight.Bold)
                    Text("Total Storage Slots: ${station.totalBatterySlots}", color = Color.Gray, fontSize = 12.sp)
                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = slotsInput,
                        onValueChange = { slotsInput = it },
                        label = { Text("Available Battery Slots (0 - ${station.totalBatterySlots})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val slots = slotsInput.toIntOrNull()
                        if (slots == null || slots < 0 || slots > station.totalBatterySlots) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Slots must be between 0 and ${station.totalBatterySlots}.")
                            }
                            return@Button
                        }

                        scope.launch {
                            isUpdatingSlots = true
                            val result = stationRepo.updateBatterySlots(station.id, slots)
                            isUpdatingSlots = false
                            selectedStationForSlots = null

                            when (result) {
                                is Resource.Success -> {
                                    loadStations()
                                    snackbarHostState.showSnackbar("Battery slots updated successfully!")
                                }
                                is Resource.Error -> {
                                    snackbarHostState.showSnackbar(result.message ?: "Failed to update slots.")
                                }
                                else -> Unit
                            }
                        }
                    },
                    enabled = !isUpdatingSlots,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    if (isUpdatingSlots) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("Save Slots")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedStationForSlots = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
