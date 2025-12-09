package com.tumbaspos.app.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.data.local.entity.SalesOrderItemEntity
import com.tumbaspos.app.domain.usecase.sales.CreateSalesOrderUseCase
import com.tumbaspos.app.domain.usecase.sales.GetProductByBarcodeUseCase
import com.tumbaspos.app.domain.usecase.sales.SearchProductsUseCase
import com.tumbaspos.app.domain.usecase.settings.GetStoreSettingsUseCase
import com.tumbaspos.app.data.local.entity.CustomerEntity
import com.tumbaspos.app.domain.manager.PrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

data class CartItem(
    val product: ProductEntity,
    val quantity: Int
) {
    val subtotal: Double get() = product.price * quantity
}

data class SalesUiState(
    val cart: List<CartItem> = emptyList(),
    val searchQuery: String = "",
    val searchResults: List<com.tumbaspos.app.data.local.dao.ProductWithCategory> = emptyList(),
    val totalAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val orderCompleted: Boolean = false,
    val customers: List<CustomerEntity> = emptyList(),
    val selectedCustomer: CustomerEntity? = null,
    val lastInvoice: String? = null,
    val lastOrder: SalesOrderEntity? = null,
    val lastOrderItems: List<CartItem> = emptyList(),
    val lastPdfPath: String? = null
)

class SalesViewModel(
    private val createSalesOrderUseCase: CreateSalesOrderUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getStoreSettingsUseCase: GetStoreSettingsUseCase,
    private val cartRepository: com.tumbaspos.app.domain.repository.CartRepository,
    private val customerRepository: com.tumbaspos.app.domain.repository.CustomerRepository,
    private val printerManager: PrinterManager,
    private val application: android.app.Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(SalesUiState())
    val uiState: StateFlow<SalesUiState> = _uiState.asStateFlow()

    init {
        observeCart()
        loadCustomers()
    }

    private fun observeCart() {
        viewModelScope.launch {
            cartRepository.cartItems.collect { items ->
                _uiState.update { 
                    it.copy(
                        cart = items,
                        totalAmount = cartRepository.getTotalAmount()
                    ) 
                }
            }
        }
    }

    private fun loadCustomers() {
        viewModelScope.launch {
            customerRepository.getAllCustomers().collect { customers ->
                val guestCustomer = customers.find { it.id == 1L } ?: customers.firstOrNull()
                _uiState.update { 
                    it.copy(
                        customers = customers,
                        selectedCustomer = it.selectedCustomer ?: guestCustomer
                    ) 
                }
            }
        }
    }
    
    fun selectCustomer(customer: CustomerEntity) {
        _uiState.update { it.copy(selectedCustomer = customer) }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isNotEmpty()) {
            viewModelScope.launch {
                searchProductsUseCase(query).collect { products ->
                    _uiState.update { it.copy(searchResults = products) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun onProductSelected(productWithCategory: com.tumbaspos.app.data.local.dao.ProductWithCategory) {
        addToCart(productWithCategory.product)
        _uiState.update { it.copy(searchQuery = "", searchResults = emptyList()) }
    }

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            val productWithCategory = getProductByBarcodeUseCase(barcode)
            if (productWithCategory != null) {
                addToCart(productWithCategory.product)
            } else {
                _uiState.update { it.copy(error = "Product not found") }
            }
        }
    }

    private fun addToCart(product: ProductEntity) {
        cartRepository.addToCart(product, 1)
    }

    fun updateQuantity(productId: Long, quantity: Int) {
        cartRepository.updateQuantity(productId, quantity)
    }

    fun removeFromCart(productId: Long) {
        cartRepository.removeFromCart(productId)
    }

    fun checkout() {
        val state = _uiState.value
        val customerId = state.selectedCustomer?.id ?: return
        if (state.cart.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val orderNumber = "ORD-${System.currentTimeMillis()}"
                val order = SalesOrderEntity(
                    orderNumber = orderNumber,
                    orderDate = System.currentTimeMillis(),
                    customerId = customerId,
                    paymentMethod = "CASH",
                    totalAmount = state.totalAmount,
                    discount = 0.0,
                    tax = 0.0,
                    status = "COMPLETED"
                )
                
                val items = state.cart.map {
                    SalesOrderItemEntity(
                        salesOrderId = 0, // Will be set by repo
                        productId = it.product.id,
                        quantity = it.quantity,
                        unitPrice = it.product.price,
                        subtotal = it.subtotal
                    )
                }

                createSalesOrderUseCase(order, items)
                cartRepository.clearCart()
                
                // Generate Invoice Text with Professional Header
                // Optimized for 58mm thermal printer (32 char width)
                val customerName = state.customers.find { it.id == customerId }?.name ?: "Unknown"
                
                // Get store settings - properly collect from Flow
                val storeSettings = getStoreSettingsUseCase().firstOrNull()
                
                // Indonesian number format (dot as thousand separator, comma as decimal)
                val indonesianFormat = java.text.DecimalFormat("#,###", 
                    java.text.DecimalFormatSymbols(java.util.Locale("id", "ID")).apply {
                        groupingSeparator = '.'
                        decimalSeparator = ','
                    }
                )
                
                val invoiceText = buildString {
                    // Header
                    appendLine("================================")
                    
                    // Logo will be rendered as actual image in PDF, not ASCII art
                    
                    appendLine("    ${storeSettings?.storeName ?: "YOUR STORE NAME HERE"}")
                    appendLine("  ${storeSettings?.storeAddress ?: "Your Street Address"}")
                    if (storeSettings?.storeAddress?.isNotEmpty() == true) {
                        // Address might be multiline, just show first line in header
                    } else {
                        appendLine("     City, Postal Code")
                    }
                    appendLine("   Phone: ${storeSettings?.storePhone ?: "Your Phone No"}")
                    appendLine("     Tax ID: ${storeSettings?.storeTaxId ?: "Your Tax ID"}")
                    appendLine("================================")
                    appendLine()
                    
                    // Transaction Details
                    appendLine("       SALES RECEIPT")
                    appendLine()
                    appendLine("Order No : $orderNumber")
                    val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(java.util.Date())
                    appendLine("Date     : $dateStr")
                    appendLine("Customer : ${customerName.take(18)}")
                    appendLine()
                    appendLine("--------------------------------")
                    
                    // Items List
                    items.forEach { item ->
                        val product = state.cart.find { it.product.id == item.productId }?.product
                        val productName = product?.name ?: "Unknown"
                        
                        // Product name (full text, wrapping allowed)
                        appendLine(productName)
                        
                        // Unit price x quantity = subtotal on one line
                        appendLine(String.format("  @ %s x %d = %s",
                            indonesianFormat.format(item.unitPrice.toLong()),
                            item.quantity,
                            indonesianFormat.format(item.subtotal.toLong())
                        ))
                        
                        appendLine() // Blank line between items
                    }
                    
                    appendLine("--------------------------------")
                    
                    // Total
                    appendLine(String.format("%-21s %9s", 
                        "TOTAL", 
                        indonesianFormat.format(state.totalAmount.toLong())
                    ))
                    appendLine("================================")
                    appendLine()
                    
                    // Footer
                    appendLine("   Thank you for shopping!")
                    appendLine("      Please come again!")
                    appendLine()
                    appendLine("================================")
                }

                // Update UI state with completed order
                _uiState.update {
                    it.copy(
                        cart = emptyList(),
                        totalAmount = 0.0,
                        orderCompleted = true,
                        lastInvoice = invoiceText,
                        lastOrder = order,
                        lastOrderItems = state.cart,
                        customers = state.customers // Preserve customers
                    )
                }
                
                // Generate TEMPORARY PDF for preview (saved to cache)
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val tempPdfPath = com.tumbaspos.app.util.ThermalReceiptPdfGenerator.generateTempPdf(
                            application,
                            invoiceText,
                            order.orderNumber,
                            storeSettings?.logoImage // Pass actual logo for PDF rendering
                        )
                        
                        if (tempPdfPath != null) {
                            _uiState.update { it.copy(lastPdfPath = tempPdfPath) }
                            android.util.Log.d("SalesViewModel", "Temp PDF generated: $tempPdfPath")
                        } else {
                            android.util.Log.e("SalesViewModel", "Failed to generate temp PDF")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SalesViewModel", "Error generating temp PDF", e)
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun printReceipt() {
        viewModelScope.launch {
            try {
                val order = _uiState.value.lastOrder ?: return@launch
                val invoiceText = _uiState.value.lastInvoice ?: return@launch
                
                _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
                
                // Get store settings for logo
                val storeSettings = getStoreSettingsUseCase().firstOrNull()
                
                // STEP 1: Generate PERMANENT PDF (save to Downloads)
                val pdfPath = withContext(Dispatchers.IO) {
                    com.tumbaspos.app.util.ThermalReceiptPdfGenerator.generatePdf(
                        application,
                        invoiceText,
                        order.orderNumber,
                        storeSettings?.logoImage // Pass actual logo for PDF rendering
                    )
                }
                
                if (pdfPath == null) {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "Failed to save PDF receipt",
                            successMessage = null,
                            lastPdfPath = null
                        ) 
                    }
                    android.util.Log.e("SalesViewModel", "Failed to save permanent PDF")
                    return@launch
                }
                
                // Update state with permanent PDF path
                _uiState.update { it.copy(lastPdfPath = pdfPath) }
                
                android.util.Log.d("SalesViewModel", "Permanent PDF saved to: $pdfPath")
                
                // STEP 2: Check if Bluetooth printer is connected
                val isBluetoothConnected = printerManager.isConnected()
                
                if (isBluetoothConnected) {
                    // STEP 3a: Print to Bluetooth printer
                    try {
                        val cartItems = _uiState.value.lastOrderItems
                        printerManager.printReceipt(order, cartItems)
                        
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = null,
                                successMessage = "Printed successfully and saved to Downloads"
                            ) 
                        }
                        android.util.Log.d("SalesViewModel", "Printed to Bluetooth printer and saved PDF")
                    } catch (e: Exception) {
                        // Bluetooth print failed, but PDF is still saved
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Print failed, but receipt saved to Downloads",
                                successMessage = null
                            ) 
                        }
                        android.util.Log.e("SalesViewModel", "Bluetooth print failed", e)
                    }
                } else {
                    // STEP 3b: Virtual printer - PDF saved to Downloads
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null,
                            successMessage = "Receipt saved to Downloads folder"
                        ) 
                    }
                    android.util.Log.d("SalesViewModel", "Virtual printer - PDF saved to Downloads")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Error saving receipt: ${e.message}",
                        successMessage = null
                    ) 
                }
                android.util.Log.e("SalesViewModel", "Error in printReceipt", e)
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
    
    fun resetOrder() {
        val customers = _uiState.value.customers
        val selectedCustomer = _uiState.value.selectedCustomer
        _uiState.value = SalesUiState(
            customers = customers,
            selectedCustomer = selectedCustomer
        )
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
