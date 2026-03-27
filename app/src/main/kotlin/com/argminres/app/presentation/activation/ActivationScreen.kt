package com.argminres.app.presentation.activation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.filled.QrCodeScanner
import com.argminres.app.presentation.scan.BarcodeScannerDialog
import androidx.compose.ui.unit.dp
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.text.input.TextFieldValue

@Composable
fun ActivationScreen(
    viewModel: ActivationViewModel = koinViewModel(),
    onActivationSuccess: () -> Unit,
    onNavigateToRestore: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showScanner by remember { mutableStateOf(false) }
    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onActivationSuccess()
        }
    }

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = androidx.compose.ui.graphics.Color(0xFF1976D2)
                    )
                    
                    Text(
                        text = "Activate Store",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    
                    Text(
                        text = "Please enter your Store ID and Activation Code to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = uiState.storeId,
                        onValueChange = {},
                        label = { Text("App ID") },
                        leadingIcon = { Icon(Icons.Default.Store, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        readOnly = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = uiState.activationCode,
                        onValueChange = viewModel::onActivationCodeChange,
                        label = { Text("Activation Code") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        placeholder = { Text("XXXX-XXXX-XXXX-XXXX") },
                        trailingIcon = {
                            IconButton(onClick = { showScanner = true }) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = "Scan QR Code")
                            }
                        }
                    )

                    if (uiState.error != null) {
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = viewModel::onActivateClick,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isInitializing,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2),
                            contentColor = androidx.compose.ui.graphics.Color.White
                        )
                    ) {
                        if (uiState.isInitializing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Initializing...")
                        } else {
                            Text("Activate")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    TextButton(
                        onClick = onNavigateToRestore,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Restore App")
                    }
                }
            }
        }

        if (showScanner) {
            BarcodeScannerDialog(
                onBarcodeScanned = { code ->
                    viewModel.onActivationCodeChange(TextFieldValue(code))
                    showScanner = false
                },
                onDismiss = { showScanner = false }
            )
        }
    }
}
