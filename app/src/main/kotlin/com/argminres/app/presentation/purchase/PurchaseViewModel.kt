package com.argminres.app.presentation.purchase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.PurchaseOrderWithItems
import com.argminres.app.data.local.dao.IngredientWithCategory
import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.data.local.entity.PurchaseOrderEntity
import com.argminres.app.data.local.entity.PurchaseOrderItemEntity
import com.argminres.app.data.local.entity.SupplierEntity
import com.argminres.app.domain.usecase.purchase.*
import com.argminres.app.domain.usecase.ingredient.SearchIngredientsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.argminres.app.data.local.entity.EmployerEntity

data class PurchaseUiState(
    val orders: List<PurchaseOrderWithItems> = emptyList(),
    val suppliers: List<SupplierEntity> = emptyList(),
    val searchResults: List<IngredientWithCategory> = emptyList(),
    val isLoading: Boolean = false,
    val isCreateOrderDialogOpen: Boolean = false,
    val isSupplierDialogOpen: Boolean = false,
    val selectedSupplier: SupplierEntity? = null,
    // Create Order State
    val newOrderSupplier: SupplierEntity? = null,
    val newOrderItems: List<PurchaseOrderItem> = emptyList(),
    val ingredientSearchQuery: String = ""
)

data class PurchaseOrderItem(
    val ingredient: IngredientEntity,
    val quantity: Double,
    val costPrice: Double
) {
    val subtotal: Double get() = costPrice * quantity
}

class PurchaseViewModel(
    private val getPurchaseOrdersUseCase: com.argminres.app.domain.usecase.purchase.GetPurchaseOrdersUseCase,
    private val createPurchaseOrderUseCase: com.argminres.app.domain.usecase.purchase.CreatePurchaseOrderUseCase,
    private val receivePurchaseOrderUseCase: com.argminres.app.domain.usecase.purchase.ReceivePurchaseOrderUseCase,
    private val getSuppliersUseCase: com.argminres.app.domain.usecase.purchase.GetSuppliersUseCase,
    private val manageSupplierUseCase: com.argminres.app.domain.usecase.purchase.ManageSupplierUseCase,
    private val searchIngredientsUseCase: SearchIngredientsUseCase,
    private val authManager: com.argminres.app.domain.manager.AuthenticationManager,
    private val auditLogger: com.argminres.app.domain.manager.AuditLogger,
    private val employerRepository: com.argminres.app.domain.repository.EmployerRepository
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
        _uiState.update { 
            it.copy(
                isCreateOrderDialogOpen = true,
                newOrderSupplier = null,
                newOrderItems = emptyList(),
                ingredientSearchQuery = ""
            )
        }
    }

    fun onDismissCreateOrderDialog() {
        _uiState.update { 
            it.copy(
                isCreateOrderDialogOpen = false,
                newOrderSupplier = null,
                newOrderItems = emptyList(),
                searchResults = emptyList(),
                ingredientSearchQuery = ""
            )
        }
    }

    fun onSelectSupplier(supplier: SupplierEntity) {
        _uiState.update { it.copy(newOrderSupplier = supplier) }
    }

    fun onIngredientSearch(query: String) {
        _uiState.update { it.copy(ingredientSearchQuery = query) }
        
        if (query.isNotBlank()) {
            viewModelScope.launch {
                searchIngredientsUseCase(query).collect { results ->
                    _uiState.update { it.copy(searchResults = results) }
                }
            }
        } else {
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
    }

    fun onAddIngredientToOrder(ingredientWithCategory: IngredientWithCategory) {
        val ingredient = ingredientWithCategory.ingredient
        val currentItems = _uiState.value.newOrderItems
        
        // Check if ingredient already exists in order
        val existingItem = currentItems.find { it.ingredient.id == ingredient.id }
        
        val updatedItems = if (existingItem != null) {
            // Increase quantity
            currentItems.map { item ->
                if (item.ingredient.id == ingredient.id) {
                    item.copy(quantity = item.quantity + 1.0)
                } else {
                    item
                }
            }
        } else {
            // Add new item
            currentItems + PurchaseOrderItem(
                ingredient = ingredient,
                quantity = 1.0,
                costPrice = ingredient.costPerUnit
            )
        }
        
        _uiState.update { 
            it.copy(
                newOrderItems = updatedItems,
                searchResults = emptyList(),
                ingredientSearchQuery = ""
            )
        }
    }

    fun onUpdateOrderItemQuantity(ingredient: IngredientEntity, newQuantity: Double) {
        val currentItems = _uiState.value.newOrderItems
        
        val updatedItems = if (newQuantity <= 0) {
            // Remove item
            currentItems.filter { it.ingredient.id != ingredient.id }
        } else {
            // Update quantity
            currentItems.map { item ->
                if (item.ingredient.id == ingredient.id) {
                    item.copy(quantity = newQuantity)
                } else {
                    item
                }
            }
        }
        
        _uiState.update { it.copy(newOrderItems = updatedItems) }
    }

    fun onSubmitOrder() {
        viewModelScope.launch {
            val state = _uiState.value
            val supplier = state.newOrderSupplier ?: return@launch
            
            val currentEmployer = authManager.getCurrentEmployer()
            
            val order = PurchaseOrderEntity(
                supplierId = supplier.id,
                orderDate = System.currentTimeMillis(),
                cashierId = currentEmployer?.id,
                status = "SUBMITTED",
                totalAmount = state.newOrderItems.sumOf { it.subtotal },
                notes = ""
            )
            
            val items = state.newOrderItems.map {
                PurchaseOrderItemEntity(
                    purchaseOrderId = 0,
                    ingredientId = it.ingredient.id,
                    quantity = it.quantity,
                    unitCost = it.costPrice,
                    subtotal = it.subtotal
                )
            }
            
            val orderId = createPurchaseOrderUseCase(order, items)
            
            // Log audit
            auditLogger.logCreate(
                entityType = "PURCHASE_ORDER",
                entityId = orderId,
                details = "Created purchase order #$orderId for supplier: ${supplier.name}"
            )
            
            onDismissCreateOrderDialog()
        }
    }

    fun onReceiveOrder(orderId: Long) {
        viewModelScope.launch {
            receivePurchaseOrderUseCase(orderId)
            
            // Log audit
            auditLogger.logUpdate(
                entityType = "PURCHASE_ORDER",
                entityId = orderId,
                details = "Received purchase order #$orderId"
            )
        }
    }

    fun onAddSupplierClick() {
        _uiState.update { 
            it.copy(
                isSupplierDialogOpen = true,
                selectedSupplier = null
            )
        }
    }

    fun onEditSupplierClick(supplier: SupplierEntity) {
        _uiState.update { 
            it.copy(
                isSupplierDialogOpen = true,
                selectedSupplier = supplier
            )
        }
    }

    fun onDismissSupplierDialog() {
        _uiState.update { 
            it.copy(
                isSupplierDialogOpen = false,
                selectedSupplier = null
            )
        }
    }

    fun onSaveSupplier(supplier: SupplierEntity) {
        viewModelScope.launch {
            if (supplier.id == 0L) {
                val newId = manageSupplierUseCase.createSupplier(supplier)
                auditLogger.logCreate(
                    entityType = "SUPPLIER",
                    entityId = newId,
                    details = "Created supplier: ${supplier.name}"
                )
            } else {
                manageSupplierUseCase.updateSupplier(supplier)
                auditLogger.logUpdate(
                    entityType = "SUPPLIER",
                    entityId = supplier.id,
                    details = "Updated supplier: ${supplier.name}"
                )
            }
            onDismissSupplierDialog()
        }
    }
}
