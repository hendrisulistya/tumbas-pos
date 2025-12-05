package com.tumbaspos.app.data.manager

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections
import com.tumbaspos.app.domain.manager.PrinterManager
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.presentation.sales.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EscPosPrinterManager(
    private val context: Context
) : PrinterManager {

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _connectedDeviceName = MutableStateFlow<String?>(null)
    override val connectedDeviceName: StateFlow<String?> = _connectedDeviceName.asStateFlow()

    private var printer: EscPosPrinter? = null

    @SuppressLint("MissingPermission")
    override suspend fun connectBluetooth(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            try {
                val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    throw Exception("Bluetooth is not enabled")
                }

                val connection = BluetoothPrintersConnections.selectFirstPaired()
                if (connection != null) {
                    // In a real app, we'd select the specific device by address. 
                    // The library's selectFirstPaired() is a simplification.
                    // To connect to a specific device, we might need to iterate or use a custom connection.
                    // For now, let's assume we are connecting to the selected one.
                    
                    // Actually, let's try to find the specific connection
                    // The library doesn't easily expose a way to create a connection from address directly 
                    // without iterating paired devices yourself or using its helper.
                    // Let's stick to a simple approach: Try to connect to the passed address.
                    
                    // Since the library's BluetoothConnection constructor takes a BluetoothDevice,
                    // let's get it from the adapter.
                    val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
                    val printerConnection = com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection(device)
                    
                    printer = EscPosPrinter(printerConnection, 203, 48f, 32)
                    _isConnected.value = true
                    _connectedDeviceName.value = device.name ?: deviceAddress
                } else {
                     throw Exception("No paired printer found")
                }
            } catch (e: Exception) {
                Log.e("PrinterManager", "Error connecting Bluetooth", e)
                _isConnected.value = false
                _connectedDeviceName.value = null
                throw e
            }
        }
    }

    override suspend fun connectUsb(usbDeviceName: String) {
        // TODO: Implement USB connection logic using UsbPrintersConnections
        // This requires handling USB permissions which is more complex with callbacks/BroadcastReceivers.
        // For this iteration, we'll focus on Bluetooth as it's more common for mobile POS.
    }

    override suspend fun disconnect() {
        printer?.disconnectPrinter()
        printer = null
        _isConnected.value = false
        _connectedDeviceName.value = null
    }

    override suspend fun printReceipt(order: SalesOrderEntity, items: List<CartItem>) {
        withContext(Dispatchers.IO) {
            val p = printer ?: throw Exception("Printer not connected")
            
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val dateStr = dateFormat.format(Date(order.orderDate))
            
            val sb = StringBuilder()
            sb.append("[C]<b><font size='big'>TumbasPOS</font></b>\n")
            sb.append("[C]================================\n")
            sb.append("[L]<b>Date:</b> $dateStr\n")
            sb.append("[L]<b>Order ID:</b> ${order.orderNumber}\n")
            sb.append("[L]<b>Customer:</b> ${order.customerId ?: "Guest"}\n")
            sb.append("[C]--------------------------------\n")
            
            items.forEach { item ->
                val total = item.product.price * item.quantity
                sb.append("[L]<b>${item.product.name}</b>\n")
                sb.append("[L]  ${item.quantity} x ${formatCurrency(item.product.price)}[R]${formatCurrency(total)}\n")
            }
            
            sb.append("[C]--------------------------------\n")
            sb.append("[L]<b>TOTAL</b>[R]<b>${formatCurrency(order.totalAmount)}</b>\n")
            sb.append("[C]================================\n")
            sb.append("[C]Thank you for your purchase!\n")
            
            p.printFormattedText(sb.toString())
        }
    }

    override suspend fun testPrint() {
        withContext(Dispatchers.IO) {
            val p = printer ?: throw Exception("Printer not connected")
            p.printFormattedText(
                "[C]<b><font size='big'>TumbasPOS</font></b>\n" +
                "[C]Printer Test Successful!\n" +
                "[C]================================\n"
            )
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)
    }
}
