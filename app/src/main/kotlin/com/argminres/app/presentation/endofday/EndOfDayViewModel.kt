package com.argminres.app.presentation.endofday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.argminres.app.data.local.entity.WasteRecordEntity
import com.argminres.app.domain.manager.AuthenticationManager
import com.argminres.app.domain.usecase.session.EndOfDayResult
import com.argminres.app.domain.usecase.session.EndOfDayUseCase
import com.argminres.app.domain.usecase.session.StartDailySessionUseCase
import com.argminres.app.domain.repository.DailySessionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EndOfDayUiState(
    val isLoading: Boolean = false,
    val wasteRecords: List<WasteRecordEntity> = emptyList(),
    val ingredientUsage: List<com.argminres.app.data.local.entity.IngredientUsageEntity> = emptyList(),
    val remainingIngredients: List<com.argminres.app.domain.usecase.session.RemainingIngredient> = emptyList(),
    val totalWaste: Double = 0.0,
    val totalIngredientCost: Double = 0.0,
    val totalSales: Double = 0.0,
    val totalProfit: Double = 0.0,
    val isProcessing: Boolean = false,
    val isComplete: Boolean = false,
    val error: String? = null,
    val hasActiveSession: Boolean = false
)

class EndOfDayViewModel(
    private val endOfDayUseCase: EndOfDayUseCase,
    private val startDailySessionUseCase: StartDailySessionUseCase,
    private val dailySessionRepository: DailySessionRepository,
    private val ingredientRepository: com.argminres.app.domain.repository.IngredientRepository,
    private val authManager: AuthenticationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(EndOfDayUiState())
    val uiState: StateFlow<EndOfDayUiState> = _uiState.asStateFlow()

    init {
        checkActiveSession()
        loadIngredients()
    }
    
    private fun loadIngredients() {
        viewModelScope.launch {
            ingredientRepository.getAllIngredients().collect { ingredients ->
                // Convert to RemainingIngredient format with starting quantities
                val remainingList = ingredients.map { ingredientWithCat ->
                    com.argminres.app.domain.usecase.session.RemainingIngredient(
                        ingredientId = ingredientWithCat.ingredient.id,
                        ingredientName = ingredientWithCat.ingredient.name,
                        startingQuantity = ingredientWithCat.ingredient.stock,
                        remainingQuantity = ingredientWithCat.ingredient.stock,
                        unit = ingredientWithCat.ingredient.unit,
                        costPerUnit = ingredientWithCat.ingredient.costPerUnit
                    )
                }
                _uiState.update { it.copy(remainingIngredients = remainingList) }
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

    fun processEndOfDay(remainingIngredients: List<com.argminres.app.domain.usecase.session.RemainingIngredient> = emptyList()) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            
            val currentEmployer = authManager.getCurrentEmployer()
            val result = endOfDayUseCase(currentEmployer?.id, remainingIngredients)
            
            when (result) {
                is EndOfDayResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            isComplete = true,
                            wasteRecords = result.wasteRecords,
                            ingredientUsage = result.ingredientUsageRecords,
                            totalWaste = result.totalWaste,
                            totalIngredientCost = result.totalIngredientCost,
                            totalSales = result.totalSales,
                            totalProfit = result.totalProfit,
                            hasActiveSession = false
                        )
                    }
                    
                    // Start new session for next day
                    startDailySessionUseCase(currentEmployer?.id)
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
