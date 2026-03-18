package com.argminres.app.presentation.ingredient

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientManagementUiState(
    val ingredients: List<IngredientEntity> = emptyList(),
    val filteredIngredients: List<IngredientEntity> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val selectedIngredient: IngredientEntity? = null,
    val error: String? = null
)

class IngredientManagementViewModel(
    private val ingredientRepository: IngredientRepository,
    private val ingredientHistoryRepository: com.argminres.app.domain.repository.IngredientHistoryRepository,
    private val dailySessionRepository: com.argminres.app.domain.repository.DailySessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(IngredientManagementUiState())
    val uiState: StateFlow<IngredientManagementUiState> = _uiState.asStateFlow()

    init {
        loadIngredients()
    }

    private fun loadIngredients() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            ingredientRepository.getIngredientsWithStock().collect { ingredients ->
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
                selectedIngredient = null
            )
        }
    }

    fun onSaveIngredient(
        name: String,
        unit: String,
        stock: Double,
        minimumStock: Double,
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
                            stock = stock,
                            minimumStock = minimumStock,
                            costPerUnit = costPerUnit,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                } else {
                    // Add new
                    ingredientRepository.insertIngredient(
                        IngredientEntity(
                            name = name,
                            unit = unit,
                            stock = stock,
                            minimumStock = minimumStock,
                            costPerUnit = costPerUnit
                        )
                    )
                }
                
                // Record in history (only for new additions with stock > 0)
                if (stock > 0) {
                    val activeSession = dailySessionRepository.getActiveSession()
                    val sessionId = activeSession?.id ?: 0L
                    val today = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
                    
                    ingredientHistoryRepository.insertHistory(
                        com.argminres.app.data.local.entity.IngredientHistoryEntity(
                            sessionId = sessionId,
                            ingredientId = ingredient?.id ?: 0,
                            ingredientName = name,
                            stockAdded = stock,
                            unit = unit,
                            sessionDate = today
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
