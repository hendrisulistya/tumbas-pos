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
    private val salesOrderRepository: com.argminres.app.domain.repository.SalesOrderRepository,
    private val reportingRepository: com.argminres.app.domain.repository.ReportingRepository
) {
    suspend operator fun invoke(
        recordedBy: Long?,
        remainingIngredients: List<EndOfDayIngredientInput> = emptyList(),
        remainingDishes: List<EndOfDayDishInput> = emptyList()
    ): EndOfDayResult {
        // Get active session
        val activeSession = dailySessionRepository.getActiveSession()
            ?: return EndOfDayResult.NoActiveSession
        
        val closedAt = System.currentTimeMillis()
        
        // 1. Process Ingredients
        val ingredientUsageRecords = mutableListOf<com.argminres.app.data.local.entity.IngredientUsageEntity>()
        val ingredientWasteRecords = mutableListOf<IngredientWasteRecordEntity>()
        val ingredientRecaps = mutableListOf<IngredientRecap>()
        var totalIngredientCost = 0.0
        var totalIngredientWasteValue = 0.0
        
        remainingIngredients.forEach { input ->
            val usedQuantity = input.startingQuantity - input.remainingQuantity - input.wastedQuantity
            val validUsedQuantity = if (usedQuantity > 0) usedQuantity else 0.0
            val totalCostForUsage = validUsedQuantity * input.costPerUnit
            
            if (validUsedQuantity > 0) {
                ingredientUsageRecords.add(
                    com.argminres.app.data.local.entity.IngredientUsageEntity(
                        sessionId = activeSession.id,
                        ingredientId = input.ingredientId,
                        ingredientName = input.ingredientName,
                        startingQuantity = input.startingQuantity,
                        remainingQuantity = input.remainingQuantity,
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

            ingredientRecaps.add(
                IngredientRecap(
                    name = input.ingredientName,
                    initial = input.startingQuantity,
                    remaining = input.remainingQuantity,
                    used = validUsedQuantity,
                    unit = input.unit
                )
            )
        }
        
        // 2. Process Dishes
        val soldQuantities = salesOrderRepository.getSoldQuantitiesByDish(activeSession.timestampStart, closedAt)
            .associateBy({ it.dishId }, { it.quantity })
            
        val wasteRecords = mutableListOf<WasteRecordEntity>()
        val dishRecaps = mutableListOf<DishRecap>()
        var totalDishWasteValue = 0.0
        
        remainingDishes.forEach { input ->
            val sold = soldQuantities[input.dishId] ?: 0
            val waste = (input.produced - sold - input.remaining).coerceAtLeast(0)
            
            if (waste > 0) {
                wasteRecords.add(
                    WasteRecordEntity(
                        sessionId = activeSession.id,
                        dishId = input.dishId,
                        dishName = input.dishName,
                        quantity = waste,
                        producedQuantity = input.produced,
                        remainingQuantity = input.remaining,
                        soldQuantity = sold,
                        reason = "UNSOLD",
                        recordedBy = recordedBy
                    )
                )
                totalDishWasteValue += waste * input.price
            }
            
            dishRecaps.add(
                DishRecap(
                    name = input.dishName,
                    produced = input.produced,
                    remaining = input.remaining,
                    sold = sold
                )
            )
        }
        
        // 3. Totals and Persistence
        val totalSales = reportingRepository.getTotalRevenue(activeSession.timestampStart, closedAt).first() ?: 0.0
        val totalProfit = totalSales - totalIngredientCost
        
        if (wasteRecords.isNotEmpty()) {
            wasteRecordRepository.createWasteRecords(wasteRecords)
        }
        
        if (ingredientUsageRecords.isNotEmpty()) {
            ingredientUsageRepository.createUsageRecords(ingredientUsageRecords)
        }
        
        if (ingredientWasteRecords.isNotEmpty()) {
            ingredientWasteRecords.forEach {
                ingredientWasteRecordRepository.insertWasteRecord(it)
            }
        }
        
        dailySessionRepository.closeSession(
            sessionId = activeSession.id,
            closedAt = closedAt,
            totalSales = totalSales,
            totalDishWasteValue = totalDishWasteValue,
            totalIngredientCost = totalIngredientCost,
            totalIngredientWasteValue = totalIngredientWasteValue,
            totalProfit = totalProfit
        )
        
        // Reset stock for all dishes involved in the session
        remainingDishes.forEach { input ->
            dishRepository.getDishById(input.dishId)?.let { d ->
                dishRepository.updateDish(d.dish.copy(stock = 0))
            }
        }
        
        return EndOfDayResult.Success(
            sessionId = activeSession.id,
            wasteRecords = wasteRecords,
            ingredientUsageRecords = ingredientUsageRecords,
            totalDishWasteValue = totalDishWasteValue,
            totalIngredientWasteValue = totalIngredientWasteValue,
            totalIngredientCost = totalIngredientCost,
            totalSales = totalSales,
            totalProfit = totalProfit,
            recap = EndOfDayRecap(ingredientRecaps, dishRecaps)
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
        val totalProfit: Double,
        val recap: EndOfDayRecap
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
        
        // Create new session starting now
        val newSession = DailySessionEntity(
            timestampStart = System.currentTimeMillis(),
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
        
        val sessionStartTime = activeSession.timestampStart
        val now = System.currentTimeMillis()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000L
        
        // Auto-close if session has been active for more than 24 hours
        // User requested to remove this limit for more flexibility
        return false
    }
}

/**
 * Data classes for manual input at end of day
 */
data class EndOfDayIngredientInput(
    val ingredientId: Long,
    val ingredientName: String,
    val startingQuantity: Double,
    val remainingQuantity: Double,
    val wastedQuantity: Double,
    val unit: String,
    val costPerUnit: Double
)

data class EndOfDayDishInput(
    val dishId: Long,
    val dishName: String,
    val produced: Int, // Current stock
    val remaining: Int, // Manual input
    val price: Double
)

data class IngredientRecap(
    val name: String,
    val initial: Double,
    val remaining: Double,
    val used: Double,
    val unit: String
)

data class DishRecap(
    val name: String,
    val produced: Int,
    val remaining: Int,
    val sold: Int
)

data class EndOfDayRecap(
    val ingredients: List<IngredientRecap>,
    val dishes: List<DishRecap>
)
