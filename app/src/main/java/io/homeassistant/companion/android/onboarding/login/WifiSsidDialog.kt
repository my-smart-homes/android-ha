package io.homeassistant.companion.android.onboarding.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WifiSsidDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var ssid by remember { mutableStateOf("msh") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Enter MSH Wi-Fi SSID") },
        text = {
            Column {
                TextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Wi-Fi SSID") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (ssid.isNotEmpty()) {
                        onConfirm(ssid)
                    }
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
} 