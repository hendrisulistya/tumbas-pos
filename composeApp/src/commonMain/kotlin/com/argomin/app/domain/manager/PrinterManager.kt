package com.argomin.app.domain.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed class PrinterPairingState {
    object Idle : PrinterPairingState()
    data class Pairing(val deviceName: String) : PrinterPairingState()
    data class Success(val deviceName: String) : PrinterPairingState()
    data class Failed(val deviceName: String, val error: String) : PrinterPairingState()
}

interface PrinterManager {
    val isConnected: StateFlow<Boolean>
    val connectedDeviceName: StateFlow<String?>
    val pairingState: StateFlow<PrinterPairingState>

    fun isConnected(): Boolean
    suspend fun smartConnect(): Boolean
    suspend fun startScan()
    suspend fun stopScan()
    suspend fun pairDevice(deviceAddress: String)
    suspend fun cancelPairing()
    suspend fun connectBluetooth(deviceAddress: String)
    suspend fun disconnect()
    suspend fun printReceipt(orderId: String, totalAmount: Long, itemsSummary: String, paymentMethod: String): Boolean
    suspend fun testPrint(): Boolean
}

class DefaultPrinterManager : PrinterManager {
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private val _pairingState = MutableStateFlow<PrinterPairingState>(PrinterPairingState.Idle)
    override val pairingState: StateFlow<PrinterPairingState> = _pairingState.asStateFlow()

    override fun isConnected(): Boolean = _isConnected.value

    override suspend fun smartConnect(): Boolean {
        _isConnected.value = true
        _connectedDeviceName.value = "Printer Thermal POS"
        return true
    }

    override suspend fun startScan() {}
    override suspend fun stopScan() {}
    override suspend fun pairDevice(deviceAddress: String) {
        _pairingState.value = PrinterPairingState.Success(deviceAddress)
    }
    override suspend fun cancelPairing() {
        _pairingState.value = PrinterPairingState.Idle
    }
    override suspend fun connectBluetooth(deviceAddress: String) {
        _isConnected.value = true
        _connectedDeviceName.value = deviceAddress
    }
    override suspend fun disconnect() {
        _isConnected.value = false
        _connectedDeviceName.value = null
    }

    override suspend fun printReceipt(
        orderId: String,
        totalAmount: Long,
        itemsSummary: String,
        paymentMethod: String
    ): Boolean {
        println("[Printer] Cetak Struk: $orderId, Total: $totalAmount, Metode: $paymentMethod, Item: $itemsSummary")
        return true
    }

    override suspend fun testPrint(): Boolean {
        println("[Printer] Test Print Berhasil")
        return true
    }
}
