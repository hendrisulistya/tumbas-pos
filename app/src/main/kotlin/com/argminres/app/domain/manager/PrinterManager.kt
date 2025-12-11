package com.argminres.app.domain.manager

import com.argminres.app.data.local.entity.SalesOrderEntity
import com.argminres.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.StateFlow

sealed class PairingState {
    object Idle : PairingState()
    data class Pairing(val deviceName: String) : PairingState()
    data class Success(val deviceName: String) : PairingState()
    data class Failed(val deviceName: String, val error: String) : PairingState()
}

interface PrinterManager {
    val isConnected: StateFlow<Boolean>
    val connectedDeviceName: StateFlow<String?>
    val pairingState: StateFlow<PairingState>
    val scannedDevices: StateFlow<List<android.bluetooth.BluetoothDevice>>
    
    fun isConnected(): Boolean
    
    suspend fun startScan()
    suspend fun stopScan()
    suspend fun pairDevice(deviceAddress: String)
    suspend fun cancelPairing()
    suspend fun connectBluetooth(deviceAddress: String)
    suspend fun connectUsb(usbDeviceName: String) // Simplified for now
    suspend fun disconnect()
    suspend fun printReceipt(order: SalesOrderEntity, items: List<CartItem>)
    suspend fun printToPdf(receiptText: String, orderNumber: String): String? // Returns file path
    suspend fun testPrint()
}
