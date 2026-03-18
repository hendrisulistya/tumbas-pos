package com.argminres.app.domain.usecase.session

import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.data.local.entity.WasteRecordEntity
import com.argminres.app.data.local.entity.IngredientWasteRecordEntity
import com.argminres.app.domain.repository.DailySessionRepository
import com.argminres.app.domain.repository.WasteRecordRepository
import com.argminres.app.domain.repository.IngredientWasteRecordRepository
import com.argminres.app.domain.repository.DishRepository
import com.argminres.app.domain.repository.SalesOrderRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar

/**
 * Use case to handle end-of-day process
 * - Records unsold dishes as waste
 * - Calculates daily totals
 * - Closes current session
 * - Resets dish stock to zero
 */
class EndOfDayUseCase(
    private val dailySessionRepository: DailySessionRepository,
    private val wasteRecordRepository: WasteRecordRepository,
    private val ingredientUsageRepository: com.argminres.app.domain.repository.IngredientUsageRepository,
    private val ingredientWasteRecordRepository: IngredientWasteRecordRepository,
    private val dishRepository: DishRepository,
    private val reportingRepository: com.argminres.app.domain.repository.ReportingRepository
) {
    suspend operator fun invoke(
        recordedBy: Long?,
        remainingIngredients: List<EndOfDayIngredientInput> = emptyList()
    ): EndOfDayResult {
        // Get active session
        val activeSession = dailySessionRepository.getActiveSession()
            ?: return EndOfDayResult.NoActiveSession
        
        // Get all dishes with stock > 0 (unsold dishes)
        val allDishes = dishRepository.getAllDishes().first()
        val unsoldDishes = allDishes.filter { it.dish.stock > 0 }
        
        // Calculate total dish waste value 
        val totalDishWasteValue = unsoldDishes.sumOf { it.dish.price * it.dish.stock }
        
        // Calculate ingredient usage and waste
        val ingredientUsageRecords = mutableListOf<com.argminres.app.data.local.entity.IngredientUsageEntity>()
        val ingredientWasteRecords = mutableListOf<IngredientWasteRecordEntity>()
        var totalIngredientCost = 0.0
        var totalIngredientWasteValue = 0.0
        
        remainingIngredients.forEach { input ->
            // Calculate used quantity based on starting, remaining, and wasted
            val usedQuantity = input.startingQuantity - input.remainingQuantity - input.wastedQuantity
            val validUsedQuantity = if (usedQuantity > 0) usedQuantity else 0.0
            
            val totalCostForUsage = validUsedQuantity * input.costPerUnit
            
            if (validUsedQuantity > 0) {
                ingredientUsageRecords.add(
                    com.argminres.app.data.local.entity.IngredientUsageEntity(
                        sessionId = activeSession.id,
                        ingredientId = input.ingredientId,
                        ingredientName = input.ingredientName,
                        quantityUsed = validUsedQuantity,
                        unit = input.unit,
                        costPerUnit = input.costPerUnit,
                        totalCost = totalCostForUsage,
                        recordedBy = recordedBy
                    )
                )
                totalIngredientCost += totalCostForUsage
            }
            
            val totalCostForWaste = input.wastedQuantity * input.costPerUnit
            if (input.wastedQuantity > 0) {
                ingredientWasteRecords.add(
                    IngredientWasteRecordEntity(
                        sessionId = activeSession.id,
                        ingredientId = input.ingredientId,
                        ingredientName = input.ingredientName,
                        quantity = input.wastedQuantity,
                        costValue = totalCostForWaste,
                        reason = "SPOILED",
                        recordedBy = recordedBy
                    )
                )
                totalIngredientWasteValue += totalCostForWaste
            }
        }
        
        // Get total sales for the session
        val totalSales = reportingRepository.getTotalRevenue(activeSession.sessionDate, System.currentTimeMillis() + 86400000).first() ?: 0.0
        
        // Calculate profit 
        val totalProfit = totalSales - totalIngredientCost
        
        // Create waste records for unsold dishes
        val wasteRecords = unsoldDishes.map { dishWithCategory ->
            WasteRecordEntity(
                sessionId = activeSession.id,
                dishId = dishWithCategory.dish.id,
                dishName = dishWithCategory.dish.name,
                quantity = dishWithCategory.dish.stock,
                reason = "UNSOLD",
                recordedBy = recordedBy
            )
        }
        
        // Save waste records
        if (wasteRecords.isNotEmpty()) {
            wasteRecordRepository.createWasteRecords(wasteRecords)
        }
        
        // Save ingredient usage and waste records
        if (ingredientUsageRecords.isNotEmpty()) {
            ingredientUsageRepository.createUsageRecords(ingredientUsageRecords)
        }
        
        if (ingredientWasteRecords.isNotEmpty()) {
            ingredientWasteRecords.forEach {
                ingredientWasteRecordRepository.insertWasteRecord(it)
            }
        }
        
        // Close the session
        dailySessionRepository.closeSession(
            sessionId = activeSession.id,
            closedAt = System.currentTimeMillis(),
            totalSales = totalSales,
            totalDishWasteValue = totalDishWasteValue,
            totalIngredientCost = totalIngredientCost,
            totalIngredientWasteValue = totalIngredientWasteValue,
            totalProfit = totalProfit
        )
        // Note: the dailySessionDao needs to be updated to support the new columns
        
        // Reset all dish stock to zero
        unsoldDishes.forEach { dishWithCategory ->
            dishRepository.updateDish(dishWithCategory.dish.copy(stock = 0))
        }
        
        return EndOfDayResult.Success(
            sessionId = activeSession.id,
            wasteRecords = wasteRecords,
            ingredientUsageRecords = ingredientUsageRecords,
            totalDishWasteValue = totalDishWasteValue,
            totalIngredientWasteValue = totalIngredientWasteValue,
            totalIngredientCost = totalIngredientCost,
            totalSales = totalSales,
            totalProfit = totalProfit
        )
    }
}

sealed class EndOfDayResult {
    data class Success(
        val sessionId: Long,
        val wasteRecords: List<WasteRecordEntity>,
        val ingredientUsageRecords: List<com.argminres.app.data.local.entity.IngredientUsageEntity>,
        val totalDishWasteValue: Double,
        val totalIngredientWasteValue: Double,
        val totalIngredientCost: Double,
        val totalSales: Double,
        val totalProfit: Double
    ) : EndOfDayResult()
    
    object NoActiveSession : EndOfDayResult()
}

/**
 * Use case to start a new daily session
 */
class StartDailySessionUseCase(
    private val dailySessionRepository: DailySessionRepository
) {
    suspend operator fun invoke(startedBy: Long?): Long {
        // Check if there's already an active session
        val activeSession = dailySessionRepository.getActiveSession()
        if (activeSession != null) {
            return activeSession.id
        }
        
        // Create new session for today
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val newSession = DailySessionEntity(
            sessionDate = calendar.timeInMillis,
            startedBy = startedBy,
            status = "ACTIVE"
        )
        
        return dailySessionRepository.createSession(newSession)
    }
}

/**
 * Use case to check if end-of-day should run automatically
 */
class CheckAutoDailyCloseUseCase(
    private val dailySessionRepository: DailySessionRepository
) {
    suspend operator fun invoke(): Boolean {
        val activeSession = dailySessionRepository.getActiveSession() ?: return false
        
        val now = Calendar.getInstance()
        val sessionDate = Calendar.getInstance().apply {
            timeInMillis = activeSession.sessionDate
        }
        
        // Check if it's 23:59 or later and still the same day
        return now.get(Calendar.HOUR_OF_DAY) == 23 && 
               now.get(Calendar.MINUTE) >= 59 &&
               now.get(Calendar.DAY_OF_YEAR) == sessionDate.get(Calendar.DAY_OF_YEAR)
    }
}

/**
 * Data class for manual ingredient input at end of day
 */
data class EndOfDayIngredientInput(
    val ingredientId: Long,
    val ingredientName: String,
    val startingQuantity: Double,
    val remainingQuantity: Double,
    val wastedQuantity: Double, // User manually inputs how much was thrown out/spoiled
    val unit: String,
    val costPerUnit: Double
)
