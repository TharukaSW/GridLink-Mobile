package com.example.gridlink.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gridlink.app.data.local.SessionManager
import com.example.gridlink.app.data.remote.ApiClient
import com.example.gridlink.app.ui.theme.GreenPrimary
import kotlinx.coroutines.launch

@Composable
fun ServerConfigDialog(
    onDismiss: () -> Unit,
    onUrlChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var urlInput by remember { mutableStateOf(sessionManager.getApiBaseUrl()) }
    var isTesting by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<Boolean?>(null) }
    var resultMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Settings, contentDescription = null, tint = GreenPrimary)
                Spacer(Modifier.width(8.dp))
                Text("Backend API Configuration")
            }
        },
        text = {
            Column {
                Text(
                    text = "Configure the C# ASP.NET Core API server URL.\n" +
                            "• Emulator: http://10.0.2.2:5015/\n" +
                            "• Physical Device: http://<your-pc-ip>:5015/",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = {
                        urlInput = it
                        testResult = null
                        resultMessage = ""
                    },
                    label = { Text("Base API URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isTesting = true
                                testResult = null
                                resultMessage = ""
                                sessionManager.setApiBaseUrl(urlInput.trim())
                                val success = ApiClient.getInstance(context).testConnection()
                                isTesting = false
                                testResult = success
                                resultMessage = if (success) "Connected successfully!" else "Could not reach server."
                            }
                        },
                        enabled = !isTesting
                    ) {
                        if (isTesting) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(6.dp))
                            Text("Pinging...")
                        } else {
                            Text("Test Connection")
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    if (testResult != null) {
                        Icon(
                            imageVector = if (testResult == true) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (testResult == true) GreenPrimary else Color.Red
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = resultMessage,
                            color = if (testResult == true) GreenPrimary else Color.Red,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    sessionManager.setApiBaseUrl(urlInput.trim())
                    onUrlChanged(urlInput.trim())
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
