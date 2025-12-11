package com.argminres.app.presentation.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ChangePinDialog(
    onDismiss: () -> Unit,
    onConfirm: (oldPin: String, newPin: String) -> Unit
) {
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var showOldPin by remember { mutableStateOf(false) }
    var showNewPin by remember { mutableStateOf(false) }
    var showConfirmPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change PIN") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Old PIN
                OutlinedTextField(
                    value = oldPin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            oldPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Current PIN") },
                    visualTransformation = if (showOldPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showOldPin = !showOldPin }) {
                            Icon(
                                if (showOldPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showOldPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // New PIN
                OutlinedTextField(
                    value = newPin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            newPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("New PIN (4 digits)") },
                    visualTransformation = if (showNewPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showNewPin = !showNewPin }) {
                            Icon(
                                if (showNewPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showNewPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Confirm New PIN
                OutlinedTextField(
                    value = confirmPin,
                    onValueChange = { 
                        if (it.length <= 4 && it.all { char -> char.isDigit() }) {
                            confirmPin = it
                            errorMessage = null
                        }
                    },
                    label = { Text("Confirm New PIN") },
                    visualTransformation = if (showConfirmPin) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    trailingIcon = {
                        IconButton(onClick = { showConfirmPin = !showConfirmPin }) {
                            Icon(
                                if (showConfirmPin) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = if (showConfirmPin) "Hide PIN" else "Show PIN"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = confirmPin.isNotEmpty() && confirmPin != newPin
                )

                // Error message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Validation hints
                if (newPin.isNotEmpty() && newPin.length < 4) {
                    Text(
                        text = "PIN must be exactly 4 digits",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                
                if (confirmPin.isNotEmpty() && confirmPin != newPin) {
                    Text(
                        text = "PINs do not match",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        oldPin.isEmpty() -> errorMessage = "Please enter your current PIN"
                        oldPin.length != 4 -> errorMessage = "Current PIN must be 4 digits"
                        newPin.isEmpty() -> errorMessage = "Please enter a new PIN"
                        newPin.length != 4 -> errorMessage = "New PIN must be exactly 4 digits"
                        confirmPin != newPin -> errorMessage = "New PINs do not match"
                        oldPin == newPin -> errorMessage = "New PIN must be different from current PIN"
                        else -> {
                            onConfirm(oldPin, newPin)
                        }
                    }
                },
                enabled = oldPin.length == 4 && newPin.length == 4 && confirmPin == newPin && oldPin != newPin
            ) {
                Text("Change PIN")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
