package com.example.gridlink.app.ui.screens.prosumer

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.remote.ReservationResponseDto
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.GreenLight
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate900
import com.example.gridlink.app.ui.theme.SolarAmber
import com.example.gridlink.app.util.DateUtils

@Composable
fun ActionSummaryScreen(
    actionType: String, // "Created", "Updated", "Cancelled"
    reservation: ReservationResponseDto,
    onNavigateToHome: () -> Unit,
    onNavigateToDetail: (id: String) -> Unit
) {
    val actionTitle = when (actionType.lowercase()) {
        "created" -> "Reservation Created Successfully"
        "updated" -> "Reservation Updated Successfully"
        "cancelled" -> "Reservation Cancelled"
        else -> "Action Completed"
    }

    val actionColor = when (actionType.lowercase()) {
        "cancelled" -> Color.Red
        "updated" -> SolarAmber
        else -> GreenPrimary
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "Action Summary",
                showLogout = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(10.dp))

            // Action Success Badge Icon
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .background(actionColor.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = actionColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Text(
                text = actionTitle,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Summary receipt for your records",
                fontSize = 13.sp,
                color = Color.Gray
            )

            // Summary Receipt Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Receipt, contentDescription = null, tint = GreenPrimary)
                            Spacer(Modifier.width(8.dp))
                            Text("Transaction Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                        StatusBadge(status = reservation.status)
                    }

                    Divider()

                    SummaryRow("Reference Number", reservation.reservationNumber)
                    SummaryRow("Solar Station", reservation.stationName)
                    SummaryRow("Trading Type", if (reservation.tradingType == "DropOff") "Solar Drop-Off (Sell)" else "Grid Charging (Buy)")
                    SummaryRow("Scheduled Time", DateUtils.formatDisplayDate(reservation.reservationDateTime))
                    SummaryRow("Requested Energy", "${reservation.requestedEnergyKWh} kWh")
                    SummaryRow("Duration", "${reservation.durationMinutes} minutes")

                    if (!reservation.cancellationReason.isNullOrBlank()) {
                        SummaryRow("Cancellation Reason", reservation.cancellationReason!!)
                    }

                    SummaryRow("Prosumer NIC", reservation.nic)
                    SummaryRow("Recorded At", DateUtils.formatDisplayDate(reservation.updatedAt ?: reservation.createdAt))
                }
            }

            Spacer(Modifier.height(10.dp))

            // Navigation Actions
            Button(
                onClick = { onNavigateToDetail(reservation.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
            ) {
                Text("View Reservation & QR Code", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onNavigateToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Return to Dashboard", fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun SummaryRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}
