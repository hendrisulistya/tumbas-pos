package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientDao {
    @Transaction
    @Query("SELECT * FROM ingredients ORDER BY name ASC")
    fun getAllIngredients(): Flow<List<IngredientWithCategory>>

    @Transaction
    @Query("SELECT * FROM ingredients WHERE id = :id")
    suspend fun getIngredientById(id: Long): IngredientWithCategory?

    @Transaction
    @Query("SELECT * FROM ingredients WHERE name LIKE '%' || :query || '%'")
    fun searchIngredients(query: String): Flow<List<IngredientWithCategory>>

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

data class IngredientWithCategory(
    @Embedded val ingredient: IngredientEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: IngredientCategoryEntity?
)

@Dao
interface IngredientCategoryDao {
    @Query("SELECT * FROM ingredient_categories ORDER BY name ASC")
    fun getAllCategories(): Flow<List<IngredientCategoryEntity>>

    @Query("SELECT * FROM ingredient_categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): IngredientCategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: IngredientCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<IngredientCategoryEntity>)

    @Update
    suspend fun updateCategory(category: IngredientCategoryEntity)

    @Delete
    suspend fun deleteCategory(category: IngredientCategoryEntity)
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
