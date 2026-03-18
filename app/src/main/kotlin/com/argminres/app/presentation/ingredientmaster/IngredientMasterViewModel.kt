package com.argminres.app.presentation.ingredientmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientMasterUiState(
    val ingredients: List<IngredientEntity> = emptyList(),
    val filteredIngredients: List<IngredientEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val showStockAdjustmentDialog: Boolean = false,
    val selectedIngredient: IngredientEntity? = null,
    val error: String? = null
)

class IngredientMasterViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientHistoryRepository: com.argminres.app.domain.repository.IngredientHistoryRepository,
    private val dailySessionRepository: com.argminres.app.domain.repository.DailySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngredientMasterUiState())
    val uiState: StateFlow<IngredientMasterUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            ingredientRepository.getAllIngredients().collect { ingredients ->
                val filtered = if (_uiState.value.searchQuery.isBlank()) {
                    ingredients
                } else {
                    ingredients.filter { 
                        it.name.contains(_uiState.value.searchQuery, ignoreCase = true)
                    }
                }
                
                _uiState.update {
                    it.copy(
                        ingredients = ingredients,
                        filteredIngredients = filtered,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        
        val filtered = if (query.isBlank()) {
            _uiState.value.ingredients
        } else {
            _uiState.value.ingredients.filter { 
                it.name.contains(query, ignoreCase = true)
            }
        }
        
        _uiState.update { it.copy(filteredIngredients = filtered) }
    }

    fun onAddIngredientClick() {
        _uiState.update { 
            it.copy(
                showAddEditDialog = true,
                selectedIngredient = null
            )
        }
    }

    fun onEditIngredientClick(ingredient: IngredientEntity) {
        _uiState.update {
            it.copy(
                showAddEditDialog = true,
                selectedIngredient = ingredient
            )
        }
    }

    fun onDialogDismiss() {
        _uiState.update {
            it.copy(
                showAddEditDialog = false,
                showStockAdjustmentDialog = false,
                selectedIngredient = null
            )
        }
    }

    fun onAdjustStockClick(ingredient: IngredientEntity) {
        _uiState.update {
            it.copy(
                showStockAdjustmentDialog = true,
                selectedIngredient = ingredient
            )
        }
    }

    fun onConfirmStockAdjustment(quantity: Double) {
        viewModelScope.launch {
            try {
                val ingredient = _uiState.value.selectedIngredient
                if (ingredient != null) {
                    ingredientRepository.updateStock(ingredient.id, quantity)
                    
                    // Record in history
                    val activeSession = dailySessionRepository.getActiveSession()
                    val sessionId = activeSession?.id ?: 0L
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    
                    ingredientHistoryRepository.insertHistory(
                        com.argminres.app.data.local.entity.IngredientHistoryEntity(
                            sessionId = sessionId,
                            ingredientId = ingredient.id,
                            ingredientName = ingredient.name,
                            stockAdded = quantity,
                            unit = ingredient.unit,
                            sessionDate = today,
                            action = if (quantity > 0) "ADDED_STOCKED" else "REMOVED_STOCKED"
                        )
                    )
                    
                    onDialogDismiss()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onSaveIngredient(
        name: String,
        unit: String,
        costPerUnit: Double
    ) {
        viewModelScope.launch {
            try {
                val ingredient = _uiState.value.selectedIngredient
                
                if (ingredient != null) {
                    // Update existing
                    ingredientRepository.updateIngredient(
                        ingredient.copy(
                            name = name,
                            unit = unit,
                            costPerUnit = costPerUnit,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Add new - start with 0 stock (will be set daily)
                    ingredientRepository.insertIngredient(
                        IngredientEntity(
                            name = name,
                            unit = unit,
                            stock = 0.0,
                            minimumStock = 0.0,
                            costPerUnit = costPerUnit
                        )
                    )
                }
                
                onDialogDismiss()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun onDeleteIngredient(ingredientId: Long) {
        viewModelScope.launch {
            try {
                val ingredient = _uiState.value.ingredients.find { it.id == ingredientId }
                if (ingredient != null) {
                    ingredientRepository.deleteIngredient(ingredient)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
