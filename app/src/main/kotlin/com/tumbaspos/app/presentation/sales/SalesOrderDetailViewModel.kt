package com.tumbaspos.app.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tumbaspos.app.data.local.entity.SalesOrderEntity
import com.tumbaspos.app.domain.manager.PrinterManager
import com.tumbaspos.app.domain.repository.ProductRepository
import com.tumbaspos.app.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SalesOrderDetailUiState(
    val order: SalesOrderEntity? = null,
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class SalesOrderDetailViewModel(
    private val salesOrderRepository: SalesOrderRepository,
    private val productRepository: ProductRepository,
    private val printerManager: PrinterManager
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

    fun printReceipt() {
        val state = _uiState.value
        if (state.order != null && state.items.isNotEmpty()) {
            viewModelScope.launch {
                try {
                    printerManager.printReceipt(state.order, state.items)
                    _uiState.update { it.copy(successMessage = "Receipt printed successfully") }
                } catch (e: Exception) {
                    _uiState.update { it.copy(error = "Printing failed: ${e.message}") }
                }
            }
        }
    }
    
    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
