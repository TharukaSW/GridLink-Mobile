package com.example.gridlink.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.ui.theme.StatusActive
import com.example.gridlink.app.ui.theme.StatusApproved
import com.example.gridlink.app.ui.theme.StatusCancelled
import com.example.gridlink.app.ui.theme.StatusCompleted
import com.example.gridlink.app.ui.theme.StatusInactive
import com.example.gridlink.app.ui.theme.StatusPending

@Composable
fun StatusBadge(status: String, modifier: Modifier = Modifier) {
    val (backgroundColor, textColor) = when (status.lowercase()) {
        "approved", "confirmed" -> Pair(StatusApproved.copy(alpha = 0.15f), StatusApproved)
        "pending" -> Pair(StatusPending.copy(alpha = 0.15f), StatusPending)
        "completed", "finalized" -> Pair(StatusCompleted.copy(alpha = 0.15f), StatusCompleted)
        "cancelled" -> Pair(StatusCancelled.copy(alpha = 0.15f), StatusCancelled)
        "active" -> Pair(StatusActive.copy(alpha = 0.15f), StatusActive)
        "deactivated", "inactive" -> Pair(StatusInactive.copy(alpha = 0.15f), StatusInactive)
        else -> Pair(Color.Gray.copy(alpha = 0.15f), Color.Gray)
    }

    Box(
        modifier = modifier
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = status.replaceFirstChar { it.uppercase() },
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
