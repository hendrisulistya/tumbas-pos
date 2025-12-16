package com.argminres.app.data.local.dao

import androidx.room.*
import com.argminres.app.data.local.entity.DishComponentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DishComponentDao {
    
    @Query("""
        SELECT componentDishId 
        FROM dish_components 
        WHERE packageDishId = :packageId
    """)
    fun getComponentDishIds(packageId: Long): Flow<List<Long>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComponent(component: DishComponentEntity)
    
    @Query("DELETE FROM dish_components WHERE id = :componentId")
    suspend fun deleteComponent(componentId: Long)
    
    @Query("DELETE FROM dish_components WHERE packageDishId = :packageId")
    suspend fun deleteAllComponentsForPackage(packageId: Long)
    
    @Query("SELECT * FROM dish_components WHERE packageDishId = :packageId")
    fun getComponentEntities(packageId: Long): Flow<List<DishComponentEntity>>
}
