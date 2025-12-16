package com.argminres.app.presentation.ingredientmaster

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.dao.IngredientWithCategory
import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IngredientMasterUiState(
    val ingredients: List<IngredientWithCategory> = emptyList(),
    val filteredIngredients: List<IngredientWithCategory> = emptyList(),
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val showAddEditDialog: Boolean = false,
    val selectedIngredient: IngredientWithCategory? = null,
    val error: String? = null
)

class IngredientMasterViewModel(
    private val ingredientRepository: IngredientRepository
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
                        it.ingredient.name.contains(_uiState.value.searchQuery, ignoreCase = true)
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
                it.ingredient.name.contains(query, ignoreCase = true)
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

    fun onEditIngredientClick(ingredient: IngredientWithCategory) {
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
        categoryId: Long,
        unit: String,
        costPerUnit: Double
    ) {
        viewModelScope.launch {
            try {
                val ingredient = _uiState.value.selectedIngredient?.ingredient
                
                if (ingredient != null) {
                    // Update existing
                    ingredientRepository.updateIngredient(
                        ingredient.copy(
                            name = name,
                            categoryId = categoryId,
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
                            categoryId = categoryId,
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
                val ingredient = _uiState.value.ingredients.find { it.ingredient.id == ingredientId }?.ingredient
                if (ingredient != null) {
                    ingredientRepository.deleteIngredient(ingredient)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
