package com.argminres.app.presentation.endofday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.WasteRecordEntity
import com.argminres.app.domain.manager.AuthenticationManager
import com.argminres.app.domain.usecase.session.EndOfDayResult
import com.argminres.app.domain.usecase.session.EndOfDayUseCase
import com.argminres.app.domain.repository.DailySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndOfDayUiState(
    val isLoading: Boolean = false,
    val currentStep: Int = 1, // 1: Bahan, 2: Hidangan, 3: Recap
    val remainingIngredients: List<com.argminres.app.domain.usecase.session.EndOfDayIngredientInput> = emptyList(),
    val remainingDishes: List<com.argminres.app.domain.usecase.session.EndOfDayDishInput> = emptyList(),
    val recap: com.argminres.app.domain.usecase.session.EndOfDayRecap? = null,
    val wasteRecords: List<WasteRecordEntity> = emptyList(),
    val ingredientUsage: List<com.argminres.app.data.local.entity.IngredientUsageEntity> = emptyList(),
    val totalDishWasteValue: Double = 0.0,
    val totalIngredientWasteValue: Double = 0.0,
    val totalIngredientCost: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val isProcessing: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null,
    val hasActiveSession: Boolean = false,
    val isManager: Boolean = false
)

class EndOfDayViewModel(
    private val endOfDayUseCase: EndOfDayUseCase,
    private val dailySessionRepository: DailySessionRepository,
    private val ingredientRepository: com.argminres.app.domain.repository.IngredientRepository,
    private val dishRepository: com.argminres.app.domain.repository.DishRepository,
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EndOfDayUiState())
    val uiState: StateFlow<EndOfDayUiState> = _uiState.asStateFlow()

    init {
        checkManagerRole()
        checkActiveSession()
        loadIngredients()
        loadDishes()
    }

    private fun checkManagerRole() {
        val currentEmployer = authManager.getCurrentEmployer()
        _uiState.update { it.copy(isManager = currentEmployer?.role == "MANAGER") }
    }
    
    private fun loadIngredients() {
        viewModelScope.launch {
            ingredientRepository.getAllIngredients().collect { ingredients ->
                val remainingList = ingredients.map { ingredient ->
                    com.argminres.app.domain.usecase.session.EndOfDayIngredientInput(
                        ingredientId = ingredient.id,
                        ingredientName = ingredient.name,
                        startingQuantity = ingredient.stock,
                        remainingQuantity = ingredient.stock,
                        wastedQuantity = 0.0,
                        unit = ingredient.unit,
                        costPerUnit = ingredient.costPerUnit
                    )
                }
                _uiState.update { it.copy(remainingIngredients = remainingList) }
            }
        }
    }

    private fun loadDishes() {
        viewModelScope.launch {
            dishRepository.getAllDishes().collect { dishes ->
                val dishesToInput = dishes
                    .filter { it.dish.stock > 0 }
                    .map { dishWithCategory ->
                        com.argminres.app.domain.usecase.session.EndOfDayDishInput(
                            dishId = dishWithCategory.dish.id,
                            dishName = dishWithCategory.dish.name,
                            produced = dishWithCategory.dish.stock,
                            remaining = dishWithCategory.dish.stock,
                            price = dishWithCategory.dish.price
                        )
                    }
                _uiState.update { it.copy(remainingDishes = dishesToInput) }
            }
        }
    }

    private fun checkActiveSession() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val activeSession = dailySessionRepository.getActiveSession()
            _uiState.update { 
                it.copy(
                    hasActiveSession = activeSession != null,
                    isLoading = false
                )
            }
        }
    }

    fun nextStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep + 1).coerceAtMost(3)) }
    }

    fun previousStep() {
        _uiState.update { it.copy(currentStep = (it.currentStep - 1).coerceAtLeast(1)) }
    }

    fun updateIngredientRemaining(id: Long, remaining: Double) {
        _uiState.update { state ->
            val updated = state.remainingIngredients.map {
                if (it.ingredientId == id) it.copy(remainingQuantity = remaining) else it
            }
            state.copy(remainingIngredients = updated)
        }
    }

    fun updateDishRemaining(id: Long, remaining: Int) {
        _uiState.update { state ->
            val updated = state.remainingDishes.map {
                if (it.dishId == id) it.copy(remaining = remaining) else it
            }
            state.copy(remainingDishes = updated)
        }
    }

    fun processEndOfDay() {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            
            val currentEmployer = authManager.getCurrentEmployer()
            val result = endOfDayUseCase(
                recordedBy = currentEmployer?.id, 
                remainingIngredients = _uiState.value.remainingIngredients,
                remainingDishes = _uiState.value.remainingDishes
            )
            
            when (result) {
                is EndOfDayResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            isComplete = true,
                            wasteRecords = result.wasteRecords,
                            ingredientUsage = result.ingredientUsageRecords,
                            totalDishWasteValue = result.totalDishWasteValue,
                            totalIngredientWasteValue = result.totalIngredientWasteValue,
                            totalIngredientCost = result.totalIngredientCost,
                            totalSales = result.totalSales,
                            totalProfit = result.totalProfit,
                            recap = result.recap,
                            hasActiveSession = false
                        )
                    }
                }
                is EndOfDayResult.NoActiveSession -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            error = "No active session found",
                            hasActiveSession = false
                        )
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { 
            EndOfDayUiState(hasActiveSession = false) 
        }
    }
}
