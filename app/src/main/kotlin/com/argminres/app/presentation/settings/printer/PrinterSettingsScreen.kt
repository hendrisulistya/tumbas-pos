package com.argminres.app.presentation.settings.printer

import android.Manifest
import android.annotation.SuppressLint
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.Print
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.argminres.app.domain.manager.PairingState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun PrinterSettingsScreen(
    viewModel: PrinterSettingsViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    val bluetoothPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    
    LaunchedEffect(Unit) {
        if (!bluetoothPermissionsState.allPermissionsGranted) {
            bluetoothPermissionsState.launchMultiplePermissionRequest()
        } else {
            viewModel.loadPairedDevices()
        }
    }
    
    // Pairing Dialog
    if (uiState.pairingState !is PairingState.Idle) {
        PairingDialog(
            pairingState = uiState.pairingState,
            onDismiss = { viewModel.cancelPairing() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Printer Settings", color = androidx.compose.ui.graphics.Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = androidx.compose.ui.graphics.Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2)
                ),
                windowInsets = WindowInsets(left = 0.dp, top = 10.dp, right = 0.dp, bottom = 0.dp)
            )
        },
        snackbarHost = { 
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(top = 80.dp)
                )
            }
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp)
        ) {
            // 1. Status Bar at the top (Optional Item)
            if (uiState.isConnecting) {
                item {
                    Surface(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Connecting to printer...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            // 2. Selected Printer Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isConnected) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Selected Printer",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    if (uiState.isConnected) Icons.Default.BluetoothConnected else Icons.Default.Print,
                                    contentDescription = null,
                                    tint = if (uiState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (uiState.isConnected) 
                                            uiState.connectedDeviceName ?: "Unknown Device"
                                        else 
                                            "Virtual PDF Printer",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (uiState.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (uiState.isConnected) "Bluetooth Printer" else "Default (Saves to PDF)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            
                            if (uiState.isConnected) {
                                Button(
                                    onClick = { viewModel.disconnect() },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Disconnect")
                                }
                            }
                        }
                        
                        if (uiState.isConnected) {
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { viewModel.testPrint() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Print, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Test Print")
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 3. Paper Size Selection
            item {
                Text(
                    text = "Paper Size",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                FilterChip(
                    selected = uiState.printerPaperSize == 58,
                    onClick = { viewModel.updatePaperSize(58) },
                    label = { Text("58mm (Default)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 4. Scale Adjustment
            item {
                Text(
                    text = "Penyesuaian Ukuran Cetak",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Scale Factor", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${"%.1f".format(uiState.printerScale)}x",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FilledIconButton(
                                onClick = { viewModel.updatePrinterScale(uiState.printerScale - 0.1f) },
                                enabled = uiState.printerScale > 0.55f, // Tolerance for float comparison
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("-", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            FilledIconButton(
                                onClick = { viewModel.updatePrinterScale(uiState.printerScale + 0.1f) },
                                enabled = uiState.printerScale < 1.95f,
                                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("+", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // 5. Bluetooth Permissions / Scanning / Device Lists
            if (!bluetoothPermissionsState.allPermissionsGranted) {
                item {
                    Button(
                        onClick = { bluetoothPermissionsState.launchMultiplePermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Grant Bluetooth Permissions")
                    }
                }
            } else if (uiState.pairedDevices.isEmpty() && uiState.scannedDevices.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No paired devices found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Button(
                            onClick = { 
                                if (bluetoothPermissionsState.allPermissionsGranted) {
                                    viewModel.startScan()
                                } else {
                                    bluetoothPermissionsState.launchMultiplePermissionRequest()
                                }
                            },
                            modifier = Modifier.padding(top = 16.dp),
                            enabled = !uiState.isConnecting && !uiState.isScanning
                        ) {
                            Text(if (uiState.isScanning) "Scanning..." else "Scan for Devices")
                        }
                    }
                }
            } else {
                // Paired Devices Section
                item {
                    Text(
                        text = "Paired Devices",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                
                items(uiState.pairedDevices) { device: android.bluetooth.BluetoothDevice ->
                    BluetoothDeviceItem(
                        device = device,
                        isConnected = uiState.isConnected && uiState.connectedDeviceName == (device.name ?: device.address),
                        isLoading = uiState.connectingDeviceAddress == device.address,
                        onClick = { 
                            if (!uiState.isConnecting) {
                                viewModel.connectBluetooth(device.address)
                            }
                        }
                    )
                }
                
                // Available Devices Section
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Available Devices",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (uiState.isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            TextButton(
                                onClick = { 
                                    if (bluetoothPermissionsState.allPermissionsGranted) {
                                        viewModel.startScan()
                                    } else {
                                        bluetoothPermissionsState.launchMultiplePermissionRequest()
                                    }
                                },
                                enabled = !uiState.isConnecting
                            ) {
                                Text("Scan")
                            }
                        }
                    }
                }
                
                if (uiState.scannedDevices.isEmpty() && !uiState.isScanning) {
                    item {
                        Text(
                            text = "No devices found. Tap Scan to search.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
                
                items(uiState.scannedDevices) { device: android.bluetooth.BluetoothDevice ->
                    BluetoothDeviceItem(
                        device = device,
                        isConnected = false,
                        isLoading = uiState.connectingDeviceAddress == device.address,
                        onClick = { 
                            if (!uiState.isConnecting) {
                                viewModel.connectBluetooth(device.address)
                            }
                        }
                    )
                }
                
                // Bottom spacing for comfortable scrolling
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}


@SuppressLint("MissingPermission")
@Composable
fun BluetoothDeviceItem(
    device: android.bluetooth.BluetoothDevice,
    isConnected: Boolean,
    isLoading: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = !isConnected && !isLoading, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(Icons.Default.Print, contentDescription = null)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name ?: "Unknown Device",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun PairingDialog(
    pairingState: PairingState,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { 
        if (pairingState !is PairingState.Pairing) {
            onDismiss()
        }
    }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (pairingState) {
                    is PairingState.Pairing -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pairing with ${pairingState.deviceName}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Please confirm the pairing request on your device",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                    }
                    is PairingState.Success -> {
                        Icon(
                            Icons.Default.BluetoothConnected,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Paired Successfully!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pairingState.deviceName,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss) {
                            Text("OK")
                        }
                    }
                    is PairingState.Failed -> {
                        Icon(
                            Icons.Default.Bluetooth,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Pairing Failed",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = pairingState.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDismiss) {
                            Text("Close")
                        }
                    }
                    is PairingState.Idle -> {
                        // Should not happen, but handle gracefully
                    }
                }
            }
        }
    }
}
