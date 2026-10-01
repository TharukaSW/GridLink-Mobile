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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.data.repository.ReservationRepository
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.util.DateUtils
import kotlinx.coroutines.launch

@Composable
fun BookingsListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (id: String) -> Unit
) {
    val context = LocalContext.current
    val reservationRepo = remember { ReservationRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var allBookings by remember { mutableStateOf<List<ReservationResponseDto>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Active & Pending, 1: History (Completed/Cancelled), 2: All
    var searchQuery by remember { mutableStateOf("") }
    var selectedTradingTypeFilter by remember { mutableStateOf<String?>(null) } // null, "DropOff", "Charging"

    fun fetchBookings() {
        scope.launch {
            isLoading = true
            when (val res = reservationRepo.getMyBookings()) {
                is Resource.Success -> {
                    allBookings = res.data ?: emptyList()
                }
                is Resource.Error -> {
                    snackbarHostState.showSnackbar(res.message ?: "Failed to load bookings.")
                }
                else -> Unit
            }
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchBookings()
    }

    // Filter logic
    val filteredBookings = allBookings.filter { booking ->
        val matchesTab = when (selectedTab) {
            0 -> booking.status.equals("Pending", ignoreCase = true) ||
                 booking.status.equals("Approved", ignoreCase = true) ||
                 booking.status.equals("Confirmed", ignoreCase = true)
            1 -> booking.status.equals("Completed", ignoreCase = true) ||
                 booking.status.equals("Cancelled", ignoreCase = true) ||
                 booking.status.equals("Finalized", ignoreCase = true)
            else -> true
        }

        val matchesSearch = if (searchQuery.isBlank()) true else {
            booking.reservationNumber.contains(searchQuery, ignoreCase = true) ||
            booking.stationName.contains(searchQuery, ignoreCase = true) ||
            booking.nic.contains(searchQuery, ignoreCase = true)
        }

        val matchesType = selectedTradingTypeFilter == null || booking.tradingType.equals(selectedTradingTypeFilter, ignoreCase = true)

        matchesTab && matchesSearch && matchesType
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "My Energy Bookings",
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
            // Tab Row (Active/Pending vs History vs All)
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Active & Pending") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("History") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("All (${allBookings.size})") }
                )
            }

            // Search Bar & Filter Chips
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by booking # or station...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GreenPrimary) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedTradingTypeFilter == null,
                            onClick = { selectedTradingTypeFilter = null },
                            label = { Text("All Types") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTradingTypeFilter == "DropOff",
                            onClick = { selectedTradingTypeFilter = if (selectedTradingTypeFilter == "DropOff") null else "DropOff" },
                            label = { Text("Drop-Off (Sell)") }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedTradingTypeFilter == "Charging",
                            onClick = { selectedTradingTypeFilter = if (selectedTradingTypeFilter == "Charging") null else "Charging" },
                            label = { Text("Charging (Buy)") }
                        )
                    }
                }
            }

            // Results count and refresh
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Showing ${filteredBookings.size} reservation(s)",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                IconButton(onClick = { fetchBookings() }, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = GreenPrimary)
                }
            }

            // Bookings List
            if (isLoading && allBookings.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenPrimary)
                }
            } else if (filteredBookings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isNotBlank() || selectedTradingTypeFilter != null)
                            "No bookings matching filter criteria."
                        else "No bookings found in this category.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredBookings) { booking ->
                        ReservationItemCard(
                            booking = booking,
                            onClick = { onNavigateToDetail(booking.id) }
                        )
                    }
                }
            }
        }
    }
}
