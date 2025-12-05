package com.tumbaspos.app.domain.manager

import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.presentation.sales.CartItem
import kotlinx.coroutines.flow.StateFlow

interface PrinterManager {
    val isConnected: StateFlow<Boolean>
    val connectedDeviceName: StateFlow<String?>
    
    suspend fun connectBluetooth(deviceAddress: String)
    suspend fun connectUsb(usbDeviceName: String) // Simplified for now
    suspend fun disconnect()
    suspend fun printReceipt(order: SalesOrderEntity, items: List<CartItem>)
    suspend fun testPrint()
}
