package com.tumbaspos.app.presentation.settings.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.domain.manager.PrinterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PrinterSettingsUiState(
    val pairedDevices: List<BluetoothDevice> = emptyList(),
    val isConnected: Boolean = false,
    val connectedDeviceName: String? = null,
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
        loadPairedDevices()
    }

    @SuppressLint("MissingPermission")
    fun loadPairedDevices() {
        try {
            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val adapter = bluetoothManager.adapter
            if (adapter != null && adapter.isEnabled) {
                _uiState.update { it.copy(pairedDevices = adapter.bondedDevices.toList()) }
            }
        } catch (e: Exception) {
            _uiState.update { it.copy(error = "Failed to load paired devices: ${e.message}") }
        }
    }

    fun connectBluetooth(deviceAddress: String) {
        viewModelScope.launch {
            try {
                printerManager.connectBluetooth(deviceAddress)
                _uiState.update { it.copy(successMessage = "Connected successfully", error = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Connection failed: ${e.message}") }
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
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
