package com.argminres.app.presentation.dishmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.DishEntity
import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DishMasterUiState(
    val dishes: List<DishWithCategory> = emptyList(),
    val filteredDishes: List<DishWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val selectedDish: DishWithCategory? = null,
    val error: String? = null
)

class DishMasterViewModel(
    private val dishRepository: DishRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DishMasterUiState())
    val uiState: StateFlow<DishMasterUiState> = _uiState.asStateFlow()

    init {
        loadDishes()
    }

    private fun loadDishes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            dishRepository.getAllDishes().collect { dishes ->
                val filtered = if (_uiState.value.searchQuery.isBlank()) {
                    dishes
                } else {
                    dishes.filter { 
                        it.dish.name.contains(_uiState.value.searchQuery, ignoreCase = true)
                    }
                }
                
                _uiState.update {
                    it.copy(
                        dishes = dishes,
                        filteredDishes = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        val filtered = if (query.isBlank()) {
            _uiState.value.dishes
        } else {
            _uiState.value.dishes.filter { 
                it.dish.name.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.update { it.copy(filteredDishes = filtered) }
    }

    fun onAddDishClick() {
        _uiState.update { 
            it.copy(
                showAddEditDialog = true,
                selectedDish = null
            )
        }
    }

    fun onEditDishClick(dish: DishWithCategory) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                selectedDish = dish
            )
        }
    }

    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                selectedDish = null
            )
        }
    }

    fun onSaveDish(
        name: String,
        categoryId: Long,
        price: Double,
        barcode: String
    ) {
        viewModelScope.launch {
            try {
                val dish = _uiState.value.selectedDish?.dish
                
                if (dish != null) {
                    // Update existing
                    dishRepository.updateDish(
                        dish.copy(
                            name = name,
                            categoryId = categoryId,
                            price = price,
                            barcode = barcode,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Add new - start with 0 stock (will be set daily)
                    dishRepository.insertDish(
                        DishEntity(
                            name = name,
                            description = "",
                            categoryId = categoryId,
                            price = price,
                            barcode = barcode,
                            stock = 0,
                            costPrice = 0.0
                        )
                    )
                }
                
                onDialogDismiss()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDeleteDish(dishId: Long) {
        viewModelScope.launch {
            try {
                val dish = _uiState.value.dishes.find { it.dish.id == dishId }?.dish
                if (dish != null) {
                    dishRepository.deleteDish(dish)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
