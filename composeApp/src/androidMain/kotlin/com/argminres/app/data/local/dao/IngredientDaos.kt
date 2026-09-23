package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<IngredientEntity>>
    
    @Query("SELECT * FROM ingredients WHERE stock > 0 ORDER BY name ASC")
    fun getIngredientsWithStock(): Flow<List<IngredientEntity>>

    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): IngredientEntity?

    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun searchIngredients(query: String): Flow<List<IngredientEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(ingredient: IngredientEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ingredients: List<IngredientEntity>)

    @Update
    suspend fun updateIngredient(ingredient: IngredientEntity)

    @Delete
    suspend fun deleteIngredient(ingredient: IngredientEntity)

    @Query("UPDATE ingredients SET stock = stock + :quantity WHERE id = :ingredientId")
    suspend fun updateStock(ingredientId: Long, quantity: Double)
}

@Dao
interface IngredientStockDao {
    @Query("SELECT * FROM ingredient_stock_movements WHERE ingredientId = :ingredientId ORDER BY createdAt DESC")
    fun getStockMovementsForIngredient(ingredientId: Long): Flow<List<IngredientStockMovementEntity>>

    @Query("SELECT * FROM ingredient_stock_movements ORDER BY createdAt DESC LIMIT 100")
    fun getRecentStockMovements(): Flow<List<IngredientStockMovementEntity>>

    @Insert
    suspend fun insertStockMovement(movement: IngredientStockMovementEntity): Long

    @Query("DELETE FROM ingredient_stock_movements WHERE ingredientId = :ingredientId")
    suspend fun deleteMovementsForIngredient(ingredientId: Long)
}
