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
import kotlinx.coroutines.launch

data class PurchaseUiState(
    val orders: List<PurchaseOrderWithItems> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val searchResults: List<ProductEntity> = emptyList(),
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
    private val getPurchaseOrdersUseCase: GetPurchaseOrdersUseCase,
    private val createPurchaseOrderUseCase: CreatePurchaseOrderUseCase,
    private val receivePurchaseOrderUseCase: ReceivePurchaseOrderUseCase,
    private val getSuppliersUseCase: GetSuppliersUseCase,
    private val manageSupplierUseCase: ManageSupplierUseCase,
    private val searchProductsUseCase: SearchProductsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PurchaseUiState())
    val uiState: StateFlow<PurchaseUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            launch {
                getPurchaseOrdersUseCase().collect { orders ->
                    _uiState.update { it.copy(orders = orders) }
                }
            }
            launch {
                getSuppliersUseCase().collect { suppliers ->
                    _uiState.update { it.copy(suppliers = suppliers) }
                }
            }
            _uiState.update { it.copy(isLoading = false) }
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

    fun onAddProductToOrder(product: ProductEntity) {
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

            createPurchaseOrderUseCase(order, items)
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
