package com.tumbaspos.app.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.data.local.entity.StoreSettingsEntity
import com.tumbaspos.app.domain.manager.PrinterManager
import com.tumbaspos.app.domain.repository.ProductRepository
import com.tumbaspos.app.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesOrderDetailUiState(
    val order: SalesOrderEntity? = null,
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val showPreviewDialog: Boolean = false,
    val previewPdfPath: String? = null,
    val invoiceText: String? = null
)

class SalesOrderDetailViewModel(
    private val salesOrderRepository: SalesOrderRepository,
    private val productRepository: ProductRepository,
    private val printerManager: PrinterManager,
    private val application: android.app.Application,
    private val getStoreSettingsUseCase: com.tumbaspos.app.domain.usecase.settings.GetStoreSettingsUseCase,
    private val employerRepository: com.tumbaspos.app.domain.repository.EmployerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesOrderDetailUiState())
    val uiState: StateFlow<SalesOrderDetailUiState> = _uiState.asStateFlow()

    fun loadOrder(orderId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val orderWithItems = salesOrderRepository.getSalesOrderById(orderId)
                if (orderWithItems != null) {
                    val cartItems = orderWithItems.items.mapNotNull { item ->
                        val productWithCategory = productRepository.getProductById(item.productId)
                        if (productWithCategory != null) {
                            CartItem(productWithCategory.product, item.quantity)
                        } else {
                            null
                        }
                    }
                    
                    _uiState.update { 
                        it.copy(
                            order = orderWithItems.order,
                            items = cartItems,
                            isLoading = false
                        ) 
                    }
                } else {
                    _uiState.update { it.copy(error = "Order not found", isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error loading order: ${e.message}", isLoading = false) }
            }
        }
    }

    fun showPreview() {
        val state = _uiState.value
        if (state.order != null && state.items.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
                    
                    // Get store settings
                    val storeSettings: StoreSettingsEntity? = getStoreSettingsUseCase().firstOrNull()
                    
                    // Indonesian number format
                    val indonesianFormat = java.text.DecimalFormat("#,###", 
                        java.text.DecimalFormatSymbols(java.util.Locale("id", "ID")).apply {
                            groupingSeparator = '.'
                            decimalSeparator = ','
                        }
                    )
                    
                    // Generate complete invoice text
                    val invoiceText = buildString {
                        // Helper function to center text for 32-char thermal receipt
                        fun centerText(text: String, width: Int = 32): String {
                            if (text.length >= width) return text.take(width)
                            val padding = (width - text.length) / 2
                            return " ".repeat(padding) + text
                        }
                        
                        // Header
                        appendLine("================================")
                        
                        // Logo will be rendered as actual image in PDF
                        
                        appendLine(centerText(storeSettings?.storeName ?: "YOUR STORE NAME HERE"))
                        appendLine(centerText(storeSettings?.storeAddress ?: "Your Street Address"))
                        if (storeSettings?.storeAddress?.isEmpty() != false) {
                            appendLine(centerText("City, Postal Code"))
                        }
                        appendLine(centerText("Phone: ${storeSettings?.storePhone ?: "Your Phone No"}"))
                        appendLine(centerText("Tax ID: ${storeSettings?.storeTaxId ?: "Your Tax ID"}"))
                        appendLine("================================")
                        appendLine()
                        
                        // Transaction Details
                        appendLine("       SALES RECEIPT")
                        appendLine()
                        appendLine("Order No : ${state.order.orderNumber}")
                        val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(state.order.orderDate))
                        appendLine("Date     : $dateStr")
                        appendLine("Customer : Guest") // TODO: Get actual customer name
                        
                        // Get cashier name
                        val cashierName = state.order.cashierId?.let { cashierId ->
                            employerRepository.getById(cashierId)?.fullName
                        } ?: "Unknown"
                        appendLine("Cashier  : $cashierName")
                        appendLine()
                        appendLine("--------------------------------")
                        
                        // Items List
                        state.items.forEach { item ->
                            // Product name
                            appendLine(item.product.name)
                            
                            // Unit price x quantity = subtotal
                            appendLine(String.format("  @ %s x %d = %s",
                                indonesianFormat.format(item.product.price.toLong()),
                                item.quantity,
                                indonesianFormat.format(item.subtotal.toLong())
                            ))
                            
                            appendLine() // Blank line between items
                        }
                        
                        appendLine("--------------------------------")
                        
                        // Total
                        appendLine(String.format("%-21s %9s", 
                            "TOTAL", 
                            indonesianFormat.format(state.order.totalAmount.toLong())
                        ))
                        appendLine("================================")
                        appendLine()
                        
                        // Footer
                        appendLine("   Thank you for shopping!")
                        appendLine("      Please come again!")
                        appendLine()
                        appendLine("================================")
                    }
                    
                    // Generate TEMP PDF for preview
                    val tempPdfPath = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.tumbaspos.app.util.ThermalReceiptPdfGenerator.generateTempPdf(
                            application,
                            invoiceText,
                            state.order.orderNumber,
                            storeSettings?.logoImage // Pass logo for PDF rendering
                        )
                    }
                    
                    if (tempPdfPath != null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                showPreviewDialog = true,
                                previewPdfPath = tempPdfPath,
                                invoiceText = invoiceText
                            ) 
                        }
                    } else {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to generate PDF preview"
                            ) 
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Error generating preview: ${e.message}"
                        ) 
                    }
                }
            }
        }
    }
    
    fun closePreview() {
        _uiState.update { it.copy(showPreviewDialog = false, previewPdfPath = null, invoiceText = null) }
    }
    
    fun printReceipt() {
        val state = _uiState.value
        if (state.order != null && state.items.isNotEmpty() && state.invoiceText != null) {
            viewModelScope.launch {
                try {
                    _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
                    
                    // Generate PERMANENT PDF (save to Downloads)
                    val pdfPath = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.tumbaspos.app.util.ThermalReceiptPdfGenerator.generatePdf(
                            application,
                            state.invoiceText,
                            state.order.orderNumber,
                            null // No logo for now
                        )
                    }
                    
                    if (pdfPath == null) {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to save PDF receipt",
                                successMessage = null
                            ) 
                        }
                        return@launch
                    }
                    
                    // Check if Bluetooth printer is connected
                    val isBluetoothConnected = printerManager.isConnected()
                    
                    if (isBluetoothConnected) {
                        // Print to Bluetooth printer
                        try {
                            printerManager.printReceipt(state.order, state.items)
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    showPreviewDialog = false,
                                    error = null,
                                    successMessage = "Printed successfully and saved to Downloads"
                                ) 
                            }
                        } catch (e: Exception) {
                            _uiState.update { 
                                it.copy(
                                    isLoading = false,
                                    showPreviewDialog = false,
                                    error = "Print failed, but receipt saved to Downloads",
                                    successMessage = null
                                ) 
                            }
                        }
                    } else {
                        // Virtual printer - PDF saved to Downloads
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                showPreviewDialog = false,
                                error = null,
                                successMessage = "Receipt saved to Downloads folder"
                            ) 
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            showPreviewDialog = false,
                            error = "Error saving receipt: ${e.message}",
                            successMessage = null
                        ) 
                    }
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
