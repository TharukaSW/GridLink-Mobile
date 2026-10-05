package com.example.gridlink.app.ui.screens.prosumer

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.data.remote.UpdateReservationDto
import com.example.gridlink.app.data.repository.ReservationRepository
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.GreenLight
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate900
import com.example.gridlink.app.ui.theme.SolarAmber
import com.example.gridlink.app.util.DateUtils
import com.example.gridlink.app.util.QRCodeHelper
import kotlinx.coroutines.launch

@Composable
fun ReservationDetailScreen(
    reservationId: String,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (actionType: String, reservation: ReservationResponseDto) -> Unit
) {
    val context = LocalContext.current
    val reservationRepo = remember { ReservationRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var booking by remember { mutableStateOf<ReservationResponseDto?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    var isCancelling by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var editEnergy by remember { mutableStateOf("") }
    var editDuration by remember { mutableIntStateOf(60) }
    var isUpdating by remember { mutableStateOf(false) }

    var ruleViolationMessage by remember { mutableStateOf<String?>(null) }

    fun loadReservation() {
        scope.launch {
            isLoading = true
            when (val res = reservationRepo.getReservationById(reservationId)) {
                is Resource.Success -> {
                    booking = res.data
                    booking?.let { b ->
                        editEnergy = b.requestedEnergyKWh.toString()
                        editDuration = b.durationMinutes
                        val qrPayload = b.qrCodeData.ifBlank { b.reservationNumber }
                        if (b.status.equals("Approved", ignoreCase = true) ||
                            b.status.equals("Confirmed", ignoreCase = true) ||
                            b.status.equals("Completed", ignoreCase = true)
                        ) {
                            qrBitmap = QRCodeHelper.generateQRCodeBitmap(qrPayload, 512)
                        }
                    }
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(res.message ?: "Failed to load reservation.")
                }
                else -> Unit
            }
            isLoading = false
        }
    }

    LaunchedEffect(reservationId) {
        loadReservation()
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "Reservation Details",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showLogout = false
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = GreenPrimary)
            }
        } else if (booking == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Reservation not found.")
            }
        } else {
            val b = booking!!
            val isActionable = b.status.equals("Pending", ignoreCase = true) ||
                               b.status.equals("Approved", ignoreCase = true) ||
                               b.status.equals("Confirmed", ignoreCase = true)

            val has12hNotice = DateUtils.has12HoursNotice(b.reservationDateTime)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Card with Booking Number and Status
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Reservation Reference", color = Color.Gray, fontSize = 12.sp)
                                Text(b.reservationNumber, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            }
                            StatusBadge(status = b.status)
                        }
                    }
                }

                // QR Code Dispatch Section (Generated once approved as specified in assignment)
                if (qrBitmap != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, tint = GreenPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Secure Transaction QR Code",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = "Present this QR code to the Grid Operator at the station to verify and finalize energy transfer.",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(16.dp))

                            Box(
                                modifier = Modifier
                                    .size(230.dp)
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = qrBitmap!!.asImageBitmap(),
                                    contentDescription = "Reservation QR Code",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Code: ${b.reservationNumber}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        }
                    }
                } else if (b.status.equals("Pending", ignoreCase = true)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SolarAmber.copy(alpha = 0.1f))
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = SolarAmber)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Transaction QR code will be generated once this reservation is approved by the station operator.",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }

                // Reservation Details Table Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Energy Transfer Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Divider()

                        DetailRow("Microgrid Hub", b.stationName)
                        DetailRow("Scheduled Date & Time", DateUtils.formatDisplayDate(b.reservationDateTime))
                        DetailRow("Trading Direction", if (b.tradingType == "DropOff") "Solar Drop-Off (Sell)" else "Grid Charging (Buy)")
                        DetailRow("Requested Energy", "${b.requestedEnergyKWh} kWh")
                        DetailRow("Duration", "${b.durationMinutes} minutes")

                        if (b.actualEnergyTransferredKWh != null && b.actualEnergyTransferredKWh!! > 0) {
                            DetailRow("Actual Transferred", "${b.actualEnergyTransferredKWh} kWh")
                        }
                        if (!b.operatorNotes.isNullOrBlank()) {
                            DetailRow("Operator Notes", b.operatorNotes!!)
                        }
                        if (!b.cancellationReason.isNullOrBlank()) {
                            DetailRow("Cancellation Reason", b.cancellationReason!!)
                        }

                        DetailRow("Prosumer NIC", b.nic)
                    }
                }

                // 12-Hour Notice Status Indicator
                if (isActionable) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (has12hNotice) GreenLight.copy(alpha = 0.1f) else Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (has12hNotice) Icons.Default.Info else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (has12hNotice) GreenPrimary else Color.Red
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (has12hNotice) "Eligible for modifications or cancellation (more than 12 hours notice remains)."
                                else "Modification/Cancellation locked (less than 12 hours before slot time).",
                                fontSize = 12.sp,
                                color = if (has12hNotice) GreenPrimary else Color.Red
                            )
                        }
                    }
                }

                // Action Buttons (Modify / Cancel)
                if (isActionable) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                if (!has12hNotice) {
                                    ruleViolationMessage = "Cannot modify reservation: The assignment guidelines require at least 12 hours advance notice before the scheduled slot time."
                                } else {
                                    showEditDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Modify")
                        }

                        Button(
                            onClick = {
                                if (!has12hNotice) {
                                    ruleViolationMessage = "Cannot cancel reservation: The assignment guidelines require at least 12 hours advance notice before the scheduled slot time."
                                } else {
                                    showCancelDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f))
                        ) {
                            Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }

    // 12-Hour Rule Violation Dialog
    if (ruleViolationMessage != null) {
        AlertDialog(
            onDismissRequest = { ruleViolationMessage = null },
            title = { Text("12-Hour Notice Rule") },
            text = { Text(ruleViolationMessage!!) },
            confirmButton = {
                Button(onClick = { ruleViolationMessage = null }) {
                    Text("Understood")
                }
            }
        )
    }

    // Cancel Dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Energy Reservation") },
            text = {
                Column {
                    Text("Please provide a reason for cancelling this energy slot reservation:", fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Cancellation Reason *") },
                        placeholder = { Text("e.g. Schedule change, weather conditions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (cancelReason.isBlank()) {
                            scope.launch { snackbarHostState.showSnackbar("Please enter a cancellation reason.") }
                            return@Button
                        }
                        scope.launch {
                            isCancelling = true
                            val result = reservationRepo.cancelReservation(reservationId, cancelReason)
                            isCancelling = false
                            showCancelDialog = false

                            when (result) {
                                is Resource.Success -> {
                                    onNavigateToSummary("Cancelled", result.data!!)
                                }
                                is Resource.Error -> {
                                    snackbarHostState.showSnackbar(result.message ?: "Failed to cancel.")
                                }
                                else -> Unit
                            }
                        }
                    },
                    enabled = !isCancelling,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("Confirm Cancellation")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Reservation")
                }
            }
        )
    }

    // Edit Dialog
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Modify Energy Reservation") },
            text = {
                Column {
                    Text("Update energy amount or duration for this booking:", fontSize = 13.sp)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = editEnergy,
                        onValueChange = { editEnergy = it },
                        label = { Text("Requested Energy (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Duration:", fontSize = 12.sp, color = Color.Gray)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(30, 60, 90, 120).forEach { mins ->
                            OutlinedButton(
                                onClick = { editDuration = mins },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (editDuration == mins) GreenPrimary.copy(alpha = 0.2f) else Color.Transparent
                                )
                            ) {
                                Text("${mins}m")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val energy = editEnergy.toDoubleOrNull() ?: 0.0
                        if (energy <= 0.0) {
                            scope.launch { snackbarHostState.showSnackbar("Enter a valid energy amount.") }
                            return@Button
                        }

                        scope.launch {
                            isUpdating = true
                            val dto = UpdateReservationDto(
                                reservationDateTime = booking!!.reservationDateTime,
                                durationMinutes = editDuration,
                                tradingType = booking!!.tradingType,
                                requestedEnergyKWh = energy,
                                stationId = booking!!.stationId
                            )
                            val result = reservationRepo.updateReservation(reservationId, dto)
                            isUpdating = false
                            showEditDialog = false

                            when (result) {
                                is Resource.Success -> {
                                    onNavigateToSummary("Updated", result.data!!)
                                }
                                is Resource.Error -> {
                                    snackbarHostState.showSnackbar(result.message ?: "Failed to update.")
                                }
                                else -> Unit
                            }
                        }
                    },
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("Save Changes")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
