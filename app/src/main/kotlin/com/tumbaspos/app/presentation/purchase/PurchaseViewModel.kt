package com.tumbaspos.app.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.dao.PurchaseOrderWithItems
import com.tumbaspos.app.data.local.entity.ProductEntity
import com.tumbaspos.app.data.local.entity.PurchaseOrderEntity
import com.tumbaspos.app.data.local.entity.PurchaseOrderItemEntity
import com.tumbaspos.app.data.local.entity.SupplierEntity
import com.tumbaspos.app.domain.usecase.purchase.*
import com.tumbaspos.app.domain.usecase.sales.SearchProductsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.tumbaspos.app.data.local.entity.EmployerEntity

data class PurchaseUiState(
    val orders: List<PurchaseOrderWithItems> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val searchResults: List<com.tumbaspos.app.data.local.dao.ProductWithCategory> = emptyList(),
    val isLoading: Boolean = false,
    val isCreateOrderDialogOpen: Boolean = false,
    val isSupplierDialogOpen: Boolean = false,
    val selectedSupplier: SupplierEntity? = null,
    // Create Order State
    val newOrderSupplier: SupplierEntity? = null,
    val newOrderItems: List<PurchaseOrderItem> = emptyList(),
    val productSearchQuery: String = ""
)

data class PurchaseOrderItem(
    val product: ProductEntity,
    val quantity: Int,
    val costPrice: Double
) {
    val subtotal: Double get() = costPrice * quantity
}

class PurchaseViewModel(
    private val getPurchaseOrdersUseCase: com.tumbaspos.app.domain.usecase.purchase.GetPurchaseOrdersUseCase,
    private val createPurchaseOrderUseCase: com.tumbaspos.app.domain.usecase.purchase.CreatePurchaseOrderUseCase,
    private val receivePurchaseOrderUseCase: com.tumbaspos.app.domain.usecase.purchase.ReceivePurchaseOrderUseCase,
    private val getSuppliersUseCase: com.tumbaspos.app.domain.usecase.purchase.GetSuppliersUseCase,
    private val manageSupplierUseCase: com.tumbaspos.app.domain.usecase.purchase.ManageSupplierUseCase,
    private val searchProductsUseCase: com.tumbaspos.app.domain.usecase.sales.SearchProductsUseCase,
    private val authManager: com.tumbaspos.app.domain.manager.AuthenticationManager,
    private val auditLogger: com.tumbaspos.app.domain.manager.AuditLogger,
    private val employerRepository: com.tumbaspos.app.domain.repository.EmployerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
        loadSuppliers()
    }

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                getPurchaseOrdersUseCase().collect { allOrders ->
                    // Get all employers for cashier name lookup
                    val employers = employerRepository.getAll().first()
                    val employerMap = employers.associateBy { it.id }
                    
                    // Filter orders based on role
                    val currentEmployer = authManager.getCurrentEmployer()
                    val filteredOrders = if (currentEmployer?.role == "CASHIER") {
                        allOrders.filter { it.order.cashierId == currentEmployer.id }
                    } else {
                        allOrders
                    }
                    
                    // Populate cashier names
                    val ordersWithCashierNames = filteredOrders.map { orderWithItems ->
                        val cashierName = orderWithItems.order.cashierId?.let { cashierId ->
                            employerMap[cashierId]?.fullName ?: "Unknown"
                        } ?: "Unknown"
                        
                        orderWithItems.copy(cashierName = cashierName)
                    }
                    
                    _uiState.update { 
                        it.copy(
                            orders = ordersWithCashierNames,
                            isLoading = false
                        )
                    }
                }
            } catch (e: Exception) {
                // Handle error if necessary
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadSuppliers() {
        viewModelScope.launch {
            getSuppliersUseCase().collect { suppliers ->
                _uiState.update { it.copy(suppliers = suppliers) }
            }
        }
    }

    fun onCreateOrderClick() {
        _uiState.update { it.copy(isCreateOrderDialogOpen = true, newOrderItems = emptyList(), newOrderSupplier = null) }
    }

    fun onDismissCreateOrderDialog() {
        _uiState.update { it.copy(isCreateOrderDialogOpen = false) }
    }

    fun onSelectSupplier(supplier: SupplierEntity) {
        _uiState.update { it.copy(newOrderSupplier = supplier) }
    }

    fun onProductSearch(query: String) {
        _uiState.update { it.copy(productSearchQuery = query) }
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

    fun onAddProductToOrder(productWithCategory: com.tumbaspos.app.data.local.dao.ProductWithCategory) {
        val product = productWithCategory.product
        _uiState.update { state ->
            val existingItem = state.newOrderItems.find { it.product.id == product.id }
            val newItems = if (existingItem != null) {
                state.newOrderItems.map {
                    if (it.product.id == product.id) it.copy(quantity = it.quantity + 1) else it
                }
            } else {
                state.newOrderItems + PurchaseOrderItem(product, 1, product.costPrice)
            }
            state.copy(newOrderItems = newItems, productSearchQuery = "", searchResults = emptyList())
        }
    }
    
    fun onUpdateOrderItemQuantity(product: ProductEntity, quantity: Int) {
        if (quantity <= 0) {
            _uiState.update { state ->
                state.copy(newOrderItems = state.newOrderItems.filter { it.product.id != product.id })
            }
        } else {
             _uiState.update { state ->
                state.copy(newOrderItems = state.newOrderItems.map { 
                    if (it.product.id == product.id) it.copy(quantity = quantity) else it 
                })
            }
        }
    }

    fun onSubmitOrder() {
        val state = _uiState.value
        val supplier = state.newOrderSupplier ?: return
        if (state.newOrderItems.isEmpty()) return

        viewModelScope.launch {
            val totalAmount = state.newOrderItems.sumOf { it.subtotal }
            val order = PurchaseOrderEntity(
                supplierId = supplier.id,
                orderDate = System.currentTimeMillis(),
                cashierId = authManager.getCurrentEmployer()?.id, // Set current cashier
                status = "SUBMITTED",
                totalAmount = totalAmount,
                notes = ""
            )
            
            val items = state.newOrderItems.map {
                PurchaseOrderItemEntity(
                    purchaseOrderId = 0,
                    productId = it.product.id,
                    quantity = it.quantity,
                    unitCost = it.costPrice,
                    subtotal = it.subtotal
                )
            }
            
            val orderId = createPurchaseOrderUseCase(order, items)
            
            // Log audit trail
            auditLogger.logAsync {
                auditLogger.logCreate(
                    "PURCHASE_ORDER",
                    orderId,
                    "Total: Rp ${String.format("%,.0f", totalAmount)}, Items: ${state.newOrderItems.size}"
                )
            }
            
            _uiState.update { it.copy(isCreateOrderDialogOpen = false) }
        }
    }

    fun onReceiveOrder(orderId: Long) {
        viewModelScope.launch {
            receivePurchaseOrderUseCase(orderId)
        }
    }
    
    // Supplier Management
    fun onAddSupplierClick() {
        _uiState.update { it.copy(isSupplierDialogOpen = true, selectedSupplier = null) }
    }
    
    fun onEditSupplierClick(supplier: SupplierEntity) {
        _uiState.update { it.copy(isSupplierDialogOpen = true, selectedSupplier = supplier) }
    }
    
    fun onDismissSupplierDialog() {
        _uiState.update { it.copy(isSupplierDialogOpen = false) }
    }
    
    fun onSaveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            if (supplier.id == 0L) {
                manageSupplierUseCase.createSupplier(supplier)
            } else {
                manageSupplierUseCase.updateSupplier(supplier)
            }
            _uiState.update { it.copy(isSupplierDialogOpen = false) }
        }
    }
}
