package com.example.gridlink.app.ui.screens.prosumer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.gridlink.app.data.remote.CreateReservationDto
import com.example.gridlink.app.data.remote.MicrogridNodeDto
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.data.repository.ReservationRepository
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.data.repository.StationRepository
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.theme.CyanElectric
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.SolarAmber
import com.example.gridlink.app.util.DateUtils
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun CreateReservationScreen(
    initialStationId: String? = null,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (actionType: String, reservation: ReservationResponseDto) -> Unit
) {
    val context = LocalContext.current
    val stationRepo = remember { StationRepository(context) }
    val reservationRepo = remember { ReservationRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var stations by remember { mutableStateOf<List<MicrogridNodeDto>>(emptyList()) }
    var selectedStation by remember { mutableStateOf<MicrogridNodeDto?>(null) }
    var stationDropdownExpanded by remember { mutableStateOf(false) }

    var tradingType by remember { mutableStateOf("DropOff") } // "DropOff" or "Charging"
    var dayOffset by remember { mutableIntStateOf(1) } // 0 to 6 days ahead (satisfies 7-day rule)
    var selectedHour by remember { mutableIntStateOf(10) } // 10:00 AM default
    var durationMinutes by remember { mutableIntStateOf(60) }
    var requestedEnergyText by remember { mutableStateOf("25.0") }

    var isLoadingStations by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoadingStations = true
        when (val res = stationRepo.getAllStations()) {
            is Resource.Success -> {
                stations = res.data ?: emptyList()
                if (initialStationId != null) {
                    selectedStation = stations.find { it.id == initialStationId }
                }
                if (selectedStation == null && stations.isNotEmpty()) {
                    selectedStation = stations.firstOrNull()
                }
            }
            else -> Unit
        }
        isLoadingStations = false
    }

    // Compute Scheduled DateTime
    val scheduledCalendar = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, dayOffset)
        set(Calendar.HOUR_OF_DAY, selectedHour)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }

    val requestedEnergy = requestedEnergyText.toDoubleOrNull() ?: 0.0
    val estimatedTariff = if (tradingType == "DropOff") requestedEnergy * 37.0 else requestedEnergy * 45.0

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "Reserve Energy Slot",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Notice Card regarding 7-Day Window and 12-Hour Cancellation Rule
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = CyanElectric.copy(alpha = 0.1f))
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = CyanElectric)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = "Rules: Energy slots must be scheduled within 7 days. Changes and cancellations require at least 12 hours advance notice.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Trading Type Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select Trading Direction", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilterChip(
                            selected = tradingType == "DropOff",
                            onClick = { tradingType = "DropOff" },
                            label = { Text("Solar Drop-Off (Sell)") },
                            leadingIcon = { Icon(Icons.Default.WbSunny, contentDescription = null, tint = SolarAmber) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = GreenPrimary.copy(alpha = 0.2f),
                                selectedLabelColor = GreenPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        FilterChip(
                            selected = tradingType == "Charging",
                            onClick = { tradingType = "Charging" },
                            label = { Text("Grid Charging (Buy)") },
                            leadingIcon = { Icon(Icons.Default.BatteryChargingFull, contentDescription = null, tint = CyanElectric) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanElectric.copy(alpha = 0.2f),
                                selectedLabelColor = CyanElectric
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Station Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Solar Microgrid Station", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))

                    if (isLoadingStations) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = GreenPrimary)
                    } else {
                        Box {
                            OutlinedTextField(
                                value = selectedStation?.let { "${it.stationName} (${it.stationCode})" } ?: "Select Station",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        modifier = Modifier.clickable { stationDropdownExpanded = true }
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { stationDropdownExpanded = true }
                            )

                            DropdownMenu(
                                expanded = stationDropdownExpanded,
                                onDismissRequest = { stationDropdownExpanded = false }
                            ) {
                                stations.forEach { station ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(station.stationName, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${station.stationCode} • ${station.address} • ${station.availableBatterySlots}/${station.totalBatterySlots} Slots",
                                                    fontSize = 11.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedStation = station
                                            stationDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    if (selectedStation != null) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Operating Hours: ${selectedStation!!.operatingHours} | Capacity: ${selectedStation!!.capacityKWh} kW/h",
                            fontSize = 12.sp,
                            color = GreenPrimary
                        )
                    }
                }
            }

            // Scheduling (Date & Time within 7 days)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, contentDescription = null, tint = GreenPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Schedule Slot (Next 7 Days)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.height(10.dp))

                    Text("Select Day (Advance Notice):", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(1 to "Tomorrow", 2 to "+2d", 3 to "+3d", 4 to "+4d", 5 to "+5d", 6 to "+6d").forEach { (offset, label) ->
                            FilterChip(
                                selected = dayOffset == offset,
                                onClick = { dayOffset = offset },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Select Time Slot Hour:", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(8 to "08:00", 10 to "10:00", 12 to "12:00", 14 to "14:00", 16 to "16:00").forEach { (hr, label) ->
                            FilterChip(
                                selected = selectedHour == hr,
                                onClick = { selectedHour = hr },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Scheduled Time: ${DateUtils.toIsoString(scheduledCalendar).replace('T', ' ')}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = GreenPrimary
                    )
                }
            }

            // Energy and Duration Specs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Energy & Duration", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = requestedEnergyText,
                        onValueChange = { requestedEnergyText = it },
                        label = { Text("Requested Energy Amount (kWh) *") },
                        leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, tint = GreenPrimary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))
                    Text("Duration (Minutes):", fontSize = 12.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(30, 60, 90, 120).forEach { mins ->
                            FilterChip(
                                selected = durationMinutes == mins,
                                onClick = { durationMinutes = mins },
                                label = { Text("${mins}m") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Est. Trading Value:", fontSize = 13.sp, color = Color.Gray)
                        Text(
                            text = "LKR ${String.format("%.2f", estimatedTariff)}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (tradingType == "DropOff") GreenPrimary else SolarAmber
                        )
                    }
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (selectedStation == null) {
                        scope.launch { snackbarHostState.showSnackbar("Please select a solar station.") }
                        return@Button
                    }
                    if (requestedEnergy <= 0.0) {
                        scope.launch { snackbarHostState.showSnackbar("Please enter a valid energy amount.") }
                        return@Button
                    }

                    if (!DateUtils.isWithin7Days(scheduledCalendar)) {
                        scope.launch { snackbarHostState.showSnackbar("Reservations must be scheduled within 7 days.") }
                        return@Button
                    }

                    scope.launch {
                        isSubmitting = true
                        val dto = CreateReservationDto(
                            stationId = selectedStation!!.id,
                            reservationDateTime = DateUtils.toIsoString(scheduledCalendar),
                            durationMinutes = durationMinutes,
                            tradingType = tradingType,
                            requestedEnergyKWh = requestedEnergy
                        )
                        val result = reservationRepo.createReservation(dto)
                        isSubmitting = false

                        when (result) {
                            is Resource.Success -> {
                                val booking = result.data!!
                                onNavigateToSummary("Created", booking)
                            }
                            is Resource.Error -> {
                                snackbarHostState.showSnackbar(result.message ?: "Failed to create reservation.")
                            }
                            else -> Unit
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Confirm Energy Slot Booking", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
