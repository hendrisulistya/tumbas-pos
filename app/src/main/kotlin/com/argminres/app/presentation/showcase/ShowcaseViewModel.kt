package com.argminres.app.presentation.showcase

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.usecase.showcase.AdjustShowcaseStockUseCase
import com.argminres.app.domain.usecase.showcase.GetShowcaseInventoryUseCase
import com.argminres.app.domain.usecase.showcase.ManageShowcaseDishUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShowcaseUiState(
    val products: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val filteredProducts: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val masterDishes: List<com.argminres.app.data.local.dao.DishWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val selectedProduct: com.argminres.app.data.local.dao.DishWithCategory? = null,
    val isStockAdjustmentDialogOpen: Boolean = false,
    val isAddDishDialogOpen: Boolean = false
)

class ShowcaseViewModel(
    private val getInventoryUseCase: GetShowcaseInventoryUseCase,
    private val adjustStockUseCase: AdjustShowcaseStockUseCase,
    private val manageDishUseCase: ManageShowcaseDishUseCase,
    private val dishHistoryRepository: com.argminres.app.domain.repository.DishHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShowcaseUiState())
    val uiState: StateFlow<ShowcaseUiState> = _uiState.asStateFlow()

    init {
        loadInventory()
        loadMasterDishes()
    }

    private fun loadInventory() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getInventoryUseCase().collect { products ->
                _uiState.update { state ->
                    state.copy(
                        products = products,
                        filteredProducts = filterProducts(products, state.searchQuery),
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            state.copy(
                searchQuery = query,
                filteredProducts = filterProducts(state.products, query)
            )
        }
    }

    private fun filterProducts(products: List<com.argminres.app.data.local.dao.DishWithCategory>, query: String): List<com.argminres.app.data.local.dao.DishWithCategory> {
        if (query.isBlank()) return products
        return products.filter {
            it.dish.name.contains(query, ignoreCase = true) ||
            it.dish.barcode.contains(query, ignoreCase = true) ||
            (it.category?.name?.contains(query, ignoreCase = true) == true)
        }
    }



    fun onStockAdjustmentClick(product: com.argminres.app.data.local.dao.DishWithCategory) {
        _uiState.update { it.copy(selectedProduct = product, isStockAdjustmentDialogOpen = true) }
    }

    fun onStockAdjustmentDialogDismiss() {
        _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
    }

    fun onConfirmStockAdjustment(quantityChange: Int, reason: String) {
        val productWithCategory = _uiState.value.selectedProduct ?: return
        viewModelScope.launch {
            adjustStockUseCase(
                dishId = productWithCategory.dish.id,
                quantityChange = quantityChange,
                reason = reason
            )
            _uiState.update { it.copy(isStockAdjustmentDialogOpen = false) }
        }
    }
    
    private fun loadMasterDishes() {
        viewModelScope.launch {
            manageDishUseCase.getAllDishes().collect { dishes ->
                _uiState.update { it.copy(masterDishes = dishes) }
            }
        }
    }
    
    fun onAddDishClick() {
        _uiState.update { it.copy(isAddDishDialogOpen = true) }
    }
    
    fun onAddDishDialogDismiss() {
        _uiState.update { it.copy(isAddDishDialogOpen = false) }
    }
    
    fun onConfirmAddDish(dishId: Long, initialStock: Int) {
        viewModelScope.launch {
            try {
                // Add stock to dish
                adjustStockUseCase(
                    dishId = dishId,
                    quantityChange = initialStock,
                    reason = "Initial stock for today",
                    movementType = "INITIAL"
                )
                
                // Record in history
                val dish = _uiState.value.masterDishes.find { it.dish.id == dishId }
                if (dish != null) {
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    dishHistoryRepository.insertHistory(
                        com.argminres.app.data.local.entity.DishHistoryEntity(
                            dishId = dishId,
                            dishName = dish.dish.name,
                            stockAdded = initialStock,
                            sessionDate = today
                        )
                    )
                }
                
                _uiState.update { it.copy(isAddDishDialogOpen = false) }
            } catch (e: Exception) {
                android.util.Log.e("ShowcaseViewModel", "Error adding dish", e)
                _uiState.update { it.copy(isAddDishDialogOpen = false) }
            }
        }
    }
}
