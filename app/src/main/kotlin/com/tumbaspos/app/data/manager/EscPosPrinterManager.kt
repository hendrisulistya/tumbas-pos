package com.tumbaspos.app.data.manager

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections
import com.tumbaspos.app.domain.manager.PrinterManager
import com.tumbaspos.app.domain.manager.PairingState
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.presentation.sales.CartItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
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

    private val _pairingState = MutableStateFlow<PairingState>(PairingState.Idle)
    override val pairingState: StateFlow<PairingState> = _pairingState.asStateFlow()

    private var printer: EscPosPrinter? = null
    private var isPairingReceiverRegistered = false

    private val _scannedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    override val scannedDevices: StateFlow<List<BluetoothDevice>> = _scannedDevices.asStateFlow()

    private val bluetoothReceiver = object : android.content.BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        val currentList = _scannedDevices.value.toMutableList()
                        if (currentList.none { d -> d.address == it.address }) {
                            currentList.add(it)
                            _scannedDevices.value = currentList
                        }
                    }
                }
            }
        }
    }

    private val pairingReceiver = object : android.content.BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE)
                    val previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.BOND_NONE)
                    
                    device?.let {
                        val deviceName = it.name ?: it.address
                        when (bondState) {
                            BluetoothDevice.BOND_BONDING -> {
                                _pairingState.value = PairingState.Pairing(deviceName)
                            }
                            BluetoothDevice.BOND_BONDED -> {
                                _pairingState.value = PairingState.Success(deviceName)
                            }
                            BluetoothDevice.BOND_NONE -> {
                                if (previousBondState == BluetoothDevice.BOND_BONDING) {
                                    _pairingState.value = PairingState.Failed(deviceName, "Pairing cancelled or failed")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun startScan() {
        withContext(Dispatchers.Main) {
            val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                throw Exception("Bluetooth is not enabled")
            }

            _scannedDevices.value = emptyList()
            
            val filter = android.content.IntentFilter(BluetoothDevice.ACTION_FOUND)
            context.registerReceiver(bluetoothReceiver, filter)
            
            if (bluetoothAdapter.isDiscovering) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter.startDiscovery()
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun stopScan() {
        withContext(Dispatchers.Main) {
            val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
            bluetoothAdapter?.cancelDiscovery()
            try {
                context.unregisterReceiver(bluetoothReceiver)
            } catch (e: IllegalArgumentException) {
                // Receiver not registered
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun connectBluetooth(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            try {
                val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    throw Exception("Bluetooth is not enabled")
                }

                // Stop discovery before connecting
                bluetoothAdapter.cancelDiscovery()

                val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
                val deviceName = device.name ?: deviceAddress
                
                // Check if device is already paired
                if (device.bondState != BluetoothDevice.BOND_BONDED) {
                    // Register pairing receiver
                    registerPairingReceiver()
                    
                    // Initiate pairing
                    _pairingState.value = PairingState.Pairing(deviceName)
                    device.createBond()
                    
                    // Wait for pairing to complete (with timeout)
                    var attempts = 0
                    val maxAttempts = 60 // 30 seconds timeout (500ms * 60)
                    while (device.bondState == BluetoothDevice.BOND_BONDING && attempts < maxAttempts) {
                        delay(500)
                        attempts++
                    }
                    
                    // Check final bond state
                    when (device.bondState) {
                        BluetoothDevice.BOND_BONDED -> {
                            _pairingState.value = PairingState.Success(deviceName)
                            delay(1000) // Brief delay to show success message
                        }
                        BluetoothDevice.BOND_NONE -> {
                            _pairingState.value = PairingState.Failed(deviceName, "Pairing was cancelled")
                            unregisterPairingReceiver()
                            throw Exception("Device pairing failed or was cancelled")
                        }
                        else -> {
                            _pairingState.value = PairingState.Failed(deviceName, "Pairing timeout")
                            unregisterPairingReceiver()
                            throw Exception("Pairing timeout")
                        }
                    }
                    
                    unregisterPairingReceiver()
                }

                // Now connect to the paired device
                val printerConnection = com.dantsu.escposprinter.connection.bluetooth.BluetoothConnection(device)
                
                printer = EscPosPrinter(printerConnection, 203, 48f, 32)
                _isConnected.value = true
                _connectedDeviceName.value = deviceName
                
                // Reset pairing state after successful connection
                _pairingState.value = PairingState.Idle
            } catch (e: Exception) {
                Log.e("PrinterManager", "Error connecting Bluetooth", e)
                _isConnected.value = false
                _connectedDeviceName.value = null
                _pairingState.value = PairingState.Idle
                throw e
            }
        }
    }

    @SuppressLint("MissingPermission")
    override suspend fun pairDevice(deviceAddress: String) {
        withContext(Dispatchers.IO) {
            try {
                val bluetoothAdapter = context.getSystemService(BluetoothManager::class.java)?.adapter
                if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                    throw Exception("Bluetooth is not enabled")
                }

                val device = bluetoothAdapter.getRemoteDevice(deviceAddress)
                val deviceName = device.name ?: deviceAddress
                
                // Check if already paired
                if (device.bondState == BluetoothDevice.BOND_BONDED) {
                    _pairingState.value = PairingState.Success(deviceName)
                    return@withContext
                }
                
                // Register pairing receiver
                registerPairingReceiver()
                
                // Initiate pairing
                _pairingState.value = PairingState.Pairing(deviceName)
                device.createBond()
                
                // Wait for pairing to complete (with timeout)
                var attempts = 0
                val maxAttempts = 60 // 30 seconds timeout
                while (device.bondState == BluetoothDevice.BOND_BONDING && attempts < maxAttempts) {
                    delay(500)
                    attempts++
                }
                
                // Check final bond state
                when (device.bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        _pairingState.value = PairingState.Success(deviceName)
                    }
                    BluetoothDevice.BOND_NONE -> {
                        _pairingState.value = PairingState.Failed(deviceName, "Pairing was cancelled")
                    }
                    else -> {
                        _pairingState.value = PairingState.Failed(deviceName, "Pairing timeout")
                    }
                }
                
                unregisterPairingReceiver()
            } catch (e: Exception) {
                Log.e("PrinterManager", "Error pairing device", e)
                _pairingState.value = PairingState.Failed("Unknown", e.message ?: "Unknown error")
                unregisterPairingReceiver()
                throw e
            }
        }
    }

    override suspend fun cancelPairing() {
        _pairingState.value = PairingState.Idle
        unregisterPairingReceiver()
    }

    private fun registerPairingReceiver() {
        if (!isPairingReceiverRegistered) {
            val filter = android.content.IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            context.registerReceiver(pairingReceiver, filter)
            isPairingReceiverRegistered = true
        }
    }

    private fun unregisterPairingReceiver() {
        if (isPairingReceiverRegistered) {
            try {
                context.unregisterReceiver(pairingReceiver)
                isPairingReceiverRegistered = false
            } catch (e: IllegalArgumentException) {
                // Receiver not registered
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
                "[C]<b><font size='big'>TestStorePOS</font></b>\n" +
                "[C]Printer Test Successful!\n" +
                "[C]================================\n"
            )
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        return java.text.NumberFormat.getCurrencyInstance(Locale("id", "ID")).format(amount)
    }
}
