package com.example.gridlink.app.ui.screens.prosumer

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.CyanElectric
import com.example.gridlink.app.ui.theme.GreenLight
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate800
import com.example.gridlink.app.ui.theme.Slate900
import com.example.gridlink.app.ui.theme.SolarAmber
import com.example.gridlink.app.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun ProsumerHomeScreen(
    onNavigateToCreateReservation: () -> Unit,
    onNavigateToBookingsList: () -> Unit,
    onNavigateToMap: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToReservationDetail: (id: String) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val reservationRepo = remember { ReservationRepository(context) }
    val scope = rememberCoroutineScope()

    val currentSession = remember { sessionManager.getSession() }
    val userNic = currentSession?.nic ?: ""
    val userName = currentSession?.fullName ?: "Solar Prosumer"

    var summary by remember { mutableStateOf<ReservationDashboardSummaryDto?>(null) }
    var recentBookings by remember { mutableStateOf<List<ReservationResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            isLoading = true
            // Load dashboard KPI counts live from API
            when (val result = reservationRepo.getDashboardSummary(userNic)) {
                is Resource.Success -> {
                    summary = result.data
                }
                else -> Unit
            }

            // Load recent bookings
            when (val result = reservationRepo.getMyBookings()) {
                is Resource.Success -> {
                    recentBookings = result.data?.take(5) ?: emptyList()
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
                title = "GridLink Prosumer",
                userRole = "Prosumer",
                showLogout = true,
                onLogoutClick = onLogout,
                onServerConfigUpdated = { loadData() }
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
            // Welcome Header Card
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
                            Column {
                                Text(
                                    text = "Welcome Back,",
                                    color = Color.LightGray,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = userName,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "NIC: $userNic",
                                    color = GreenLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            IconButton(onClick = { loadData() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh",
                                    tint = GreenLight
                                )
                            }
                        }
                    }
                }
            }

            // Live Dashboard Operational KPI Cards (as specified in PDF)
            item {
                Text(
                    text = "Operational Dashboard",
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
                        title = "Pending",
                        count = summary?.pendingCount ?: 0,
                        icon = Icons.Default.HourglassTop,
                        accentColor = SolarAmber,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Approved Future",
                        count = summary?.approvedFutureCount ?: 0,
                        icon = Icons.Default.CheckCircle,
                        accentColor = GreenLight,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Total Bookings",
                        count = summary?.totalReservations ?: 0,
                        icon = Icons.Default.DateRange,
                        accentColor = CyanElectric,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions Section
            item {
                Text(
                    text = "Quick Actions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "New Booking",
                        subtitle = "Reserve Slot",
                        icon = Icons.Default.Add,
                        backgroundColor = GreenPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToCreateReservation
                    )
                    ActionTile(
                        title = "Nearby Hubs",
                        subtitle = "Google Maps",
                        icon = Icons.Default.Map,
                        backgroundColor = CyanElectric,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToMap
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        title = "My Bookings",
                        subtitle = "History & QR",
                        icon = Icons.Default.QrCode,
                        backgroundColor = SolarAmber,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToBookingsList
                    )
                    ActionTile(
                        title = "My Profile",
                        subtitle = "Account Details",
                        icon = Icons.Default.Person,
                        backgroundColor = Slate800,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToProfile
                    )
                }
            }

            // Recent Bookings Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Energy Reservations",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View All",
                        color = GreenPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToBookingsList() }
                    )
                }
            }

            // Recent Bookings List
            if (isLoading && recentBookings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = GreenPrimary)
                    }
                }
            } else if (recentBookings.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No reservations found.", color = Color.Gray, fontSize = 14.sp)
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = onNavigateToCreateReservation,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                Text("Book First Slot")
                            }
                        }
                    }
                }
            } else {
                items(recentBookings) { booking ->
                    ReservationItemCard(
                        booking = booking,
                        onClick = { onNavigateToReservationDetail(booking.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    count: Long,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = count.toString(),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 11.sp,
                color = Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun ReservationItemCard(
    booking: ReservationResponseDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = booking.reservationNumber,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                StatusBadge(status = booking.status)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                text = booking.stationName,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = GreenPrimary
            )

            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${booking.tradingType} • ${booking.requestedEnergyKWh} kWh",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = DateUtils.formatDisplayDate(booking.reservationDateTime),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
