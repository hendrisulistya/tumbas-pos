package com.argminres.app.presentation.settings.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.domain.manager.PrinterManager
import com.argminres.app.domain.manager.PairingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrinterSettingsUiState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val scannedDevices: List<BluetoothDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isConnecting: Boolean = false,
    val connectingDeviceAddress: String? = null, // Track which device is being connected
    val isConnected: Boolean = false,
    val connectedDeviceName: String? = null,
    val pairingState: PairingState = PairingState.Idle,
    val error: String? = null,
    val successMessage: String? = null
)

class PrinterSettingsViewModel(
    private val printerManager: PrinterManager,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrinterSettingsUiState())
    val uiState: StateFlow<PrinterSettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            printerManager.isConnected.collect { isConnected ->
                _uiState.update { it.copy(isConnected = isConnected) }
            }
        }
        viewModelScope.launch {
            printerManager.connectedDeviceName.collect { name ->
                _uiState.update { it.copy(connectedDeviceName = name) }
            }
        }
        viewModelScope.launch {
            printerManager.scannedDevices.collect { devices ->
                _uiState.update { it.copy(scannedDevices = devices) }
            }
        }
        viewModelScope.launch {
            printerManager.pairingState.collect { pairingState ->
                _uiState.update { it.copy(pairingState = pairingState) }
            }
        }
        loadPairedDevices()
    }

    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (adapter != null && adapter.isEnabled) {
                // Filter only printer devices
                val printerDevices = adapter.bondedDevices.filter { device ->
                    isPrinterDevice(device)
                }
                _uiState.update { it.copy(pairedDevices = printerDevices) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to load paired devices: ${e.message}") }
        }
    }
    
    /**
     * Check if a Bluetooth device is likely a printer
     * Based on device class and name patterns
     */
    private fun isPrinterDevice(device: BluetoothDevice): Boolean {
        // Check device class
        device.bluetoothClass?.let { btClass ->
            // Major device class for imaging devices (printers, scanners, etc.)
            val majorDeviceClass = btClass.majorDeviceClass
            
            // 0x0600 = Imaging (printer, scanner, camera, display)
            if (majorDeviceClass == 0x0600) {
                return true
            }
            
            // Some printers identify as "Peripheral" (0x0500)
            if (majorDeviceClass == 0x0500) {
                // Check device name for printer keywords
                return isLikelyPrinterByName(device)
            }
        }
        
        // Fallback: check device name for printer keywords
        return isLikelyPrinterByName(device)
    }
    
    @SuppressLint("MissingPermission")
    private fun isLikelyPrinterByName(device: BluetoothDevice): Boolean {
        val name = device.name?.lowercase() ?: return false
        val printerKeywords = listOf(
            "printer", "print", "pos", "receipt", 
            "thermal", "epson", "star", "bixolon",
            "citizen", "zebra", "tsc", "xprinter"
        )
        return printerKeywords.any { keyword -> name.contains(keyword) }
    }

    fun startScan() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isScanning = true) }
                printerManager.startScan()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Scan failed: ${e.message}", isScanning = false) }
            }
        }
    }

    fun stopScan() {
        viewModelScope.launch {
            try {
                printerManager.stopScan()
                _uiState.update { it.copy(isScanning = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Stop scan failed: ${e.message}") }
            }
        }
    }

    fun connectBluetooth(deviceAddress: String) {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(
                    isConnecting = true, 
                    connectingDeviceAddress = deviceAddress,
                    error = null, 
                    successMessage = null
                ) }
                printerManager.connectBluetooth(deviceAddress)
                _uiState.update { it.copy(
                    isConnecting = false, 
                    connectingDeviceAddress = null,
                    successMessage = "Connected successfully", 
                    error = null
                ) }
                loadPairedDevices() // Refresh paired list after potential pairing
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isConnecting = false, 
                    connectingDeviceAddress = null,
                    error = "Connection failed: ${e.message}"
                ) }
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            printerManager.disconnect()
            _uiState.update { it.copy(successMessage = "Disconnected", error = null) }
        }
    }

    fun testPrint() {
        viewModelScope.launch {
            try {
                printerManager.testPrint()
                _uiState.update { it.copy(successMessage = "Test print sent", error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Test print failed: ${e.message}") }
            }
        }
    }
    
    fun pairDevice(deviceAddress: String) {
        viewModelScope.launch {
            try {
                printerManager.pairDevice(deviceAddress)
                loadPairedDevices() // Refresh paired list after pairing
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Pairing failed: ${e.message}") }
            }
        }
    }
    
    fun cancelPairing() {
        viewModelScope.launch {
            printerManager.cancelPairing()
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
