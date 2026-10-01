package com.example.gridlink.app.ui.screens.operator

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.data.repository.ReservationRepository
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.screens.prosumer.DetailRow
import com.example.gridlink.app.ui.theme.GreenLight
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate900
import com.example.gridlink.app.ui.theme.SolarAmber
import com.example.gridlink.app.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun OperatorQrScannerScreen(
    onNavigateBack: () -> Unit,
    onJobFinalized: (ReservationResponseDto) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val reservationRepo = remember { ReservationRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var qrCodeInput by remember { mutableStateOf("") }
    var isVerifying by remember { mutableStateOf(false) }
    var verifiedReservation by remember { mutableStateOf<ReservationResponseDto?>(null) }

    var actualEnergyText by remember { mutableStateOf("") }
    var operatorNotes by remember { mutableStateOf("") }
    var isFinalizing by remember { mutableStateOf(false) }
    var isJobCompleted by remember { mutableStateOf(false) }

    fun verifyQr(code: String) {
        if (code.isBlank()) {
            scope.launch { snackbarHostState.showSnackbar("Please enter or scan a QR code payload.") }
            return
        }

        scope.launch {
            isVerifying = true
            when (val res = reservationRepo.verifyQrCode(code.trim())) {
                is Resource.Success -> {
                    verifiedReservation = res.data
                    actualEnergyText = res.data?.requestedEnergyKWh?.toString() ?: "25.0"
                    snackbarHostState.showSnackbar("Reservation verified successfully with server!")
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(res.message ?: "Failed to verify QR code against server.")
                }
                else -> Unit
            }
            isVerifying = false
        }
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "QR Dispatch & Verification",
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Scanner / Camera View Section
            if (verifiedReservation == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Slate900)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Live QR Code Scanner",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Point camera at the Prosumer's transaction QR code",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        Spacer(Modifier.height(14.dp))

                        // CameraX Preview Container
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .background(Color.Black, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    val previewView = PreviewView(ctx)
                                    try {
                                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                                        cameraProviderFuture.addListener({
                                            try {
                                                val cameraProvider = cameraProviderFuture.get()
                                                val preview = Preview.Builder().build()
                                                preview.setSurfaceProvider(previewView.surfaceProvider)
                                                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                                                cameraProvider.unbindAll()
                                                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }, ContextCompat.getMainExecutor(ctx))
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                    previewView
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Reticle Overlay
                            Box(
                                modifier = Modifier
                                    .size(160.dp)
                                    .background(Color.Transparent, RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(Modifier.height(16.dp))

                        // Manual / Quick QR Input for Testing & Emulator Support
                        OutlinedTextField(
                            value = qrCodeInput,
                            onValueChange = { qrCodeInput = it },
                            label = { Text("QR Data or Booking #") },
                            placeholder = { Text("e.g. RES-202610-0001 or scan data") },
                            leadingIcon = { Icon(Icons.Default.QrCode, contentDescription = null, tint = GreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = { verifyQr(qrCodeInput) },
                            enabled = !isVerifying,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            if (isVerifying) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            } else {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Verify with Server")
                            }
                        }
                    }
                }
            }

            // Server-Verified Reservation Details
            if (verifiedReservation != null && !isJobCompleted) {
                val b = verifiedReservation!!

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = GreenPrimary)
                                Spacer(Modifier.width(8.dp))
                                Text("Server Verified Booking", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            StatusBadge(status = b.status)
                        }

                        Divider()

                        DetailRow("Booking Number", b.reservationNumber)
                        DetailRow("Prosumer Name", b.prosumerName)
                        DetailRow("Prosumer NIC", b.nic)
                        DetailRow("Station", b.stationName)
                        DetailRow("Scheduled Time", DateUtils.formatDisplayDate(b.reservationDateTime))
                        DetailRow("Trading Direction", if (b.tradingType == "DropOff") "Solar Drop-Off (Sell)" else "Grid Charging (Buy)")
                        DetailRow("Requested Energy", "${b.requestedEnergyKWh} kWh")

                        Spacer(Modifier.height(6.dp))
                        Divider()
                        Spacer(Modifier.height(6.dp))

                        // Finalization Form
                        Text("Finalize Energy Transfer", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                        OutlinedTextField(
                            value = actualEnergyText,
                            onValueChange = { actualEnergyText = it },
                            label = { Text("Actual Energy Transferred (kWh) *") },
                            leadingIcon = { Icon(Icons.Default.FlashOn, contentDescription = null, tint = SolarAmber) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = operatorNotes,
                            onValueChange = { operatorNotes = it },
                            label = { Text("Operator Notes / Battery Slot #") },
                            leadingIcon = { Icon(Icons.Default.Note, contentDescription = null, tint = GreenPrimary) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                val actualKwh = actualEnergyText.toDoubleOrNull() ?: 0.0
                                if (actualKwh <= 0.0) {
                                    scope.launch { snackbarHostState.showSnackbar("Please enter a valid transferred energy amount.") }
                                    return@Button
                                }

                                scope.launch {
                                    isFinalizing = true
                                    val result = reservationRepo.finalizeReservation(
                                        id = b.id,
                                        actualEnergyKWh = actualKwh,
                                        operatorNotes = operatorNotes.ifBlank { "Verified and finalized by Grid Operator via Android Mobile app." }
                                    )
                                    isFinalizing = false

                                    when (result) {
                                        is Resource.Success -> {
                                            isJobCompleted = true
                                            verifiedReservation = result.data
                                            snackbarHostState.showSnackbar("Energy transfer finalized successfully!")
                                        }
                                        is Resource.Error -> {
                                            snackbarHostState.showSnackbar(result.message ?: "Failed to finalize energy transfer.")
                                        }
                                        else -> Unit
                                    }
                                }
                            },
                            enabled = !isFinalizing,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            if (isFinalizing) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Complete Transfer & Mark Job as Done", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
                            onClick = { verifiedReservation = null },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Scan Another QR Code")
                        }
                    }
                }
            }

            // Job Completed Card
            if (isJobCompleted && verifiedReservation != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(GreenLight.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(36.dp))
                        }

                        Text("Job Completed & Finalized", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Energy transfer for booking ${verifiedReservation!!.reservationNumber} has been finalized on the central server.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Divider()

                        DetailRow("Actual Transferred", "${verifiedReservation!!.actualEnergyTransferredKWh} kWh")
                        DetailRow("Finalized By", verifiedReservation!!.finalizedBy ?: "Grid Operator")
                        DetailRow("Updated Status", verifiedReservation!!.status)

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = {
                                isJobCompleted = false
                                verifiedReservation = null
                                qrCodeInput = ""
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                        ) {
                            Text("Scan Next Prosumer QR")
                        }

                        OutlinedButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to Operator Dashboard")
                        }
                    }
                }
            }
        }
    }
}
