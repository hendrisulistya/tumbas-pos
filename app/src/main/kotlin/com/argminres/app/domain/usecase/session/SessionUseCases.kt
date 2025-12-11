package com.argminres.app.domain.usecase.session

import com.argminres.app.data.local.entity.DailySessionEntity
import com.argminres.app.data.local.entity.WasteRecordEntity
import com.argminres.app.domain.repository.DailySessionRepository
import com.argminres.app.domain.repository.WasteRecordRepository
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
    private val dishRepository: DishRepository,
    private val salesOrderRepository: SalesOrderRepository
) {
    suspend operator fun invoke(
        recordedBy: Long?,
        remainingIngredients: List<RemainingIngredient> = emptyList()
    ): EndOfDayResult {
        // Get active session
        val activeSession = dailySessionRepository.getActiveSession()
            ?: return EndOfDayResult.NoActiveSession
        
        // Get all dishes with stock > 0 (unsold dishes)
        val allDishes = dishRepository.getAllDishes().first()
        val unsoldDishes = allDishes.filter { it.dish.stock > 0 }
        
        // Calculate totals
        val totalWasteLoss = unsoldDishes.sumOf { it.dish.stock * it.dish.costPrice }
        
        // Get total sales for the session
        // Note: This would need to be implemented based on your sales tracking
        val totalSales = 0.0 // TODO: Calculate from sales orders for this session
        
        // Calculate profit (simplified - would need ingredient costs)
        val totalProfit = totalSales - totalWasteLoss
        
        // Create waste records for unsold dishes
        val wasteRecords = unsoldDishes.map { dishWithCategory ->
            WasteRecordEntity(
                sessionId = activeSession.id,
                dishId = dishWithCategory.dish.id,
                dishName = dishWithCategory.dish.name,
                quantity = dishWithCategory.dish.stock,
                costPrice = dishWithCategory.dish.costPrice,
                totalLoss = dishWithCategory.dish.stock * dishWithCategory.dish.costPrice,
                reason = "UNSOLD",
                recordedBy = recordedBy
            )
        }
        
        // Save waste records
        if (wasteRecords.isNotEmpty()) {
            wasteRecordRepository.createWasteRecords(wasteRecords)
        }
        
        // Track ingredient usage from manual input
        // Manager inputs remaining ingredients, we calculate usage
        val ingredientUsageRecords = remainingIngredients.map { remaining ->
            com.argminres.app.data.local.entity.IngredientUsageEntity(
                sessionId = activeSession.id,
                ingredientId = remaining.ingredientId,
                ingredientName = remaining.ingredientName,
                quantityUsed = remaining.startingQuantity - remaining.remainingQuantity,
                unit = remaining.unit,
                costPerUnit = remaining.costPerUnit,
                totalCost = (remaining.startingQuantity - remaining.remainingQuantity) * remaining.costPerUnit,
                recordedBy = recordedBy
            )
        }
        
        // Save ingredient usage records
        if (ingredientUsageRecords.isNotEmpty()) {
            ingredientUsageRepository.createUsageRecords(ingredientUsageRecords)
        }
        
        val totalIngredientCost = ingredientUsageRecords.sumOf { it.totalCost }
        
        // Close the session
        dailySessionRepository.closeSession(
            sessionId = activeSession.id,
            closedAt = System.currentTimeMillis(),
            totalSales = totalSales,
            totalWaste = totalWasteLoss,
            totalProfit = totalProfit
        )
        
        // Reset all dish stock to zero
        unsoldDishes.forEach { dishWithCategory ->
            dishRepository.updateDish(dishWithCategory.dish.copy(stock = 0))
        }
        
        return EndOfDayResult.Success(
            sessionId = activeSession.id,
            wasteRecords = wasteRecords,
            ingredientUsageRecords = ingredientUsageRecords,
            totalWaste = totalWasteLoss,
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
        val totalWaste: Double,
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
data class RemainingIngredient(
    val ingredientId: Long,
    val ingredientName: String,
    val startingQuantity: Double,
    val remainingQuantity: Double,
    val unit: String,
    val costPerUnit: Double
)
