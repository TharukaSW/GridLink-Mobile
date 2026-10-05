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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.local.SessionManager
import com.example.gridlink.app.data.remote.ReservationDashboardSummaryDto
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.data.repository.ReservationRepository
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.screens.prosumer.ActionTile
import com.example.gridlink.app.ui.screens.prosumer.KpiCard
import com.example.gridlink.app.ui.screens.prosumer.ReservationItemCard
import com.example.gridlink.app.ui.theme.CyanElectric
import com.example.gridlink.app.ui.theme.GreenLight
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate900
import com.example.gridlink.app.ui.theme.SolarAmber
import kotlinx.coroutines.launch

@Composable
fun OperatorHomeScreen(
    onNavigateToQrScanner: () -> Unit,
    onNavigateToStations: () -> Unit,
    onNavigateToReservations: () -> Unit,
    onNavigateToReservationDetail: (id: String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val reservationRepo = remember { ReservationRepository(context) }
    val scope = rememberCoroutineScope()

    val currentSession = remember { sessionManager.getSession() }
    val operatorName = currentSession?.fullName ?: "Grid Operator"
    val operatorNic = currentSession?.nic ?: ""

    var summary by remember { mutableStateOf<ReservationDashboardSummaryDto?>(null) }
    var pendingReservations by remember { mutableStateOf<List<ReservationResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            isLoading = true
            when (val res = reservationRepo.getDashboardSummary(null)) {
                is Resource.Success -> {
                    summary = res.data
                }
                else -> Unit
            }

            when (val res = reservationRepo.getAllReservations(status = "Approved")) {
                is Resource.Success -> {
                    pendingReservations = res.data?.take(6) ?: emptyList()
                }
                else -> Unit
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "GridLink Operator",
                userRole = "GridOperator",
                showLogout = true,
                onLogoutClick = onLogout
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Operator Header Card
            item {
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .background(SolarAmber.copy(alpha = 0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = SolarAmber)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text("Operator Control Hub", color = Color.Gray, fontSize = 12.sp)
                                    Text(operatorName, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    Text("NIC: $operatorNic", color = SolarAmber, fontSize = 12.sp)
                                }
                            }
                            IconButton(onClick = { loadData() }) {
                                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = SolarAmber)
                            }
                        }
                    }
                }
            }

            // Operator System Overview KPIs
            item {
                Text(
                    text = "System Operational Overview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiCard(
                        title = "Pending Verifications",
                        count = summary?.approvedFutureCount ?: 0,
                        icon = Icons.Default.HourglassTop,
                        accentColor = SolarAmber,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Completed Trades",
                        count = summary?.completedCount ?: 0,
                        icon = Icons.Default.CheckCircle,
                        accentColor = GreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Total System Bookings",
                        count = summary?.totalReservations ?: 0,
                        icon = Icons.Default.ListAlt,
                        accentColor = CyanElectric,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Primary Operator Actions
            item {
                Text(
                    text = "Operator Tasks",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))

                ActionTile(
                    title = "Scan Transaction QR Code",
                    subtitle = "Verify Prosumer & Finalize Energy Transfer",
                    icon = Icons.Default.QrCodeScanner,
                    backgroundColor = GreenPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onNavigateToQrScanner
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "Battery Slots",
                        subtitle = "Update Storage",
                        icon = Icons.Default.BatteryChargingFull,
                        backgroundColor = SolarAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToStations
                    )

                    ActionTile(
                        title = "Reservations",
                        subtitle = "Monitor Trading",
                        icon = Icons.Default.ListAlt,
                        backgroundColor = Slate900,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToReservations
                    )
                }
            }

            // Ready For Transfer Queue Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Approved Slots Awaiting QR Dispatch",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Scan QR",
                        color = GreenPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToQrScanner() }
                    )
                }
            }

            if (isLoading && pendingReservations.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            } else if (pendingReservations.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("No pending transfers in queue.", color = Color.Gray, fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(pendingReservations) { booking ->
                    ReservationItemCard(
                        booking = booking,
                        onClick = { onNavigateToReservationDetail(booking.id) }
                    )
                }
            }
        }
    }
}
