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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    val orderCompleted: Boolean = false,
    val customers: List<CustomerEntity> = emptyList(),
    val lastInvoice: String? = null
)

class SalesViewModel(
    private val createSalesOrderUseCase: CreateSalesOrderUseCase,
    private val getProductByBarcodeUseCase: GetProductByBarcodeUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val getStoreSettingsUseCase: GetStoreSettingsUseCase,
    private val cartRepository: com.tumbaspos.app.domain.repository.CartRepository,
    private val customerRepository: com.tumbaspos.app.domain.repository.CustomerRepository
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
                _uiState.update { it.copy(customers = customers) }
            }
        }
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

    fun checkout(customerId: Long) {
        val state = _uiState.value
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
                
                // Get store settings
                val storeSettings = getStoreSettingsUseCase().stateIn(
                    viewModelScope,
                    SharingStarted.WhileSubscribed(5000),
                    null
                ).value
                
                // Indonesian number format (dot as thousand separator, comma as decimal)
                val indonesianFormat = java.text.DecimalFormat("#,###", 
                    java.text.DecimalFormatSymbols(java.util.Locale("id", "ID")).apply {
                        groupingSeparator = '.'
                        decimalSeparator = ','
                    }
                )
                
                val invoiceText = buildString {
                    // Header with Logo Placeholder
                    appendLine("================================")
                    appendLine("          [LOGO]")
                    appendLine()
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

                _uiState.update { 
                    SalesUiState(
                        orderCompleted = true, 
                        lastInvoice = invoiceText,
                        customers = state.customers // Preserve customers
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun resetOrder() {
        val customers = _uiState.value.customers
        _uiState.value = SalesUiState(customers = customers)
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
