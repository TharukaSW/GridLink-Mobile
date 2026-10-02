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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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
import com.example.gridlink.app.data.repository.ProsumerRepository
import com.example.gridlink.app.data.repository.Resource
import com.example.gridlink.app.ui.components.GridLinkAppBar
import com.example.gridlink.app.ui.components.StatusBadge
import com.example.gridlink.app.ui.theme.GreenPrimary
import com.example.gridlink.app.ui.theme.Slate900
import kotlinx.coroutines.launch

@Composable
fun ProsumerProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val prosumerRepo = remember { ProsumerRepository(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var session by remember { mutableStateOf(sessionManager.getSession()) }
    val nic = session?.nic ?: ""

    var isEditMode by remember { mutableStateOf(false) }
    var editFullName by remember { mutableStateOf(session?.fullName ?: "") }
    var editEmail by remember { mutableStateOf(session?.email ?: "") }
    var editPhone by remember { mutableStateOf(session?.phoneNumber ?: "") }
    var editAddress by remember { mutableStateOf(session?.address ?: "") }

    var isSaving by remember { mutableStateOf(false) }
    var showDeactivateDialog by remember { mutableStateOf(false) }
    var isDeactivating by remember { mutableStateOf(false) }

    fun refreshProfile() {
        scope.launch {
            when (val res = prosumerRepo.getProfile(nic)) {
                is Resource.Success -> {
                    res.data?.let { user ->
                        editFullName = user.fullName
                        editEmail = user.email
                        editPhone = user.phoneNumber
                        editAddress = user.address
                        session = sessionManager.getSession()
                    }
                }
                else -> Unit
            }
        }
    }

    LaunchedEffect(nic) {
        if (nic.isNotBlank()) refreshProfile()
    }

    Scaffold(
        topBar = {
            GridLinkAppBar(
                title = "Prosumer Profile",
                showBackButton = true,
                onBackClick = onNavigateBack,
                showLogout = true,
                onLogoutClick = onLogout
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
            // Profile Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Slate900)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .background(GreenPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = session?.fullName ?: "Solar Prosumer",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = "NIC (Primary Key): $nic",
                        fontSize = 13.sp,
                        color = Color.LightGray
                    )

                    Spacer(Modifier.height(8.dp))

                    StatusBadge(status = session?.status ?: "Active")
                }
            }

            // Profile Details & Edit Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Personal Information", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        if (!isEditMode) {
                            TextButton(onClick = { isEditMode = true }) {
                                Text("Edit Profile", color = GreenPrimary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Divider()

                    if (isEditMode) {
                        OutlinedTextField(
                            value = editFullName,
                            onValueChange = { editFullName = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editEmail,
                            onValueChange = { editEmail = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = GreenPrimary) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = editAddress,
                            onValueChange = { editAddress = it },
                            label = { Text("Physical Address / Solar Property") },
                            leadingIcon = { Icon(Icons.Default.Home, contentDescription = null, tint = GreenPrimary) },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditMode = false },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    scope.launch {
                                        isSaving = true
                                        val result = prosumerRepo.updateProfile(
                                            nic = nic,
                                            fullName = editFullName,
                                            email = editEmail,
                                            phone = editPhone,
                                            address = editAddress
                                        )
                                        isSaving = false

                                        when (result) {
                                            is Resource.Success -> {
                                                isEditMode = false
                                                session = sessionManager.getSession()
                                                snackbarHostState.showSnackbar("Profile updated successfully!")
                                            }
                                            is Resource.Error -> {
                                                snackbarHostState.showSnackbar(result.message ?: "Failed to update profile.")
                                            }
                                            else -> Unit
                                        }
                                    }
                                },
                                enabled = !isSaving,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                            ) {
                                if (isSaving) {
                                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                                } else {
                                    Text("Save")
                                }
                            }
                        }
                    } else {
                        DetailItem(Icons.Default.Badge, "NIC", nic)
                        DetailItem(Icons.Default.Person, "Full Name", session?.fullName ?: "")
                        DetailItem(Icons.Default.Email, "Email", session?.email ?: "")
                        DetailItem(Icons.Default.Phone, "Phone", session?.phoneNumber ?: "")
                        DetailItem(Icons.Default.Home, "Address", session?.address ?: "")
                    }
                }
            }

            // Account Control & Deactivation Request (from Assignment PDF)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Account Management", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Divider()

                    Text(
                        text = "According to microgrid guidelines, prosumers may submit an account deactivation request. Deactivated accounts can only be reactivated by a Backoffice officer via the web portal.",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(Modifier.height(4.dp))

                    Button(
                        onClick = { showDeactivateDialog = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.85f))
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Request Account Deactivation")
                    }
                }
            }

            // Local SQLite Session Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.08f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Native SQLite Persistence Active",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Text(
                        text = "Local user session and reference data are securely stored in pure native Android SQLite database (no frameworks).",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    // Deactivation Dialog
    if (showDeactivateDialog) {
        AlertDialog(
            onDismissRequest = { showDeactivateDialog = false },
            title = { Text("Request Account Deactivation?") },
            text = {
                Text(
                    "Are you sure you want to request deactivation for NIC $nic?\n\n" +
                    "Your request will be recorded on the central server. Once deactivated, only a Backoffice officer can reactivate your profile."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            isDeactivating = true
                            val result = prosumerRepo.requestDeactivation(nic)
                            isDeactivating = false
                            showDeactivateDialog = false

                            when (result) {
                                is Resource.Success -> {
                                    session = sessionManager.getSession()
                                    snackbarHostState.showSnackbar(result.data ?: "Deactivation requested successfully.")
                                }
                                is Resource.Error -> {
                                    snackbarHostState.showSnackbar(result.message ?: "Failed to submit request.")
                                }
                                else -> Unit
                            }
                        }
                    },
                    enabled = !isDeactivating,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    if (isDeactivating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White)
                    } else {
                        Text("Confirm Request")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeactivateDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = GreenPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = Color.Gray, fontSize = 11.sp)
            Text(value.ifBlank { "N/A" }, fontWeight = FontWeight.Medium, fontSize = 13.sp)
        }
    }
}
