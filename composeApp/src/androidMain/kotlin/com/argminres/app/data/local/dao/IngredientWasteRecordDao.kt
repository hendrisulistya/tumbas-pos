package com.argminres.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.argminres.app.data.local.entity.IngredientWasteRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IngredientWasteRecordDao {
    @Query("SELECT * FROM ingredient_waste_records WHERE sessionId = :sessionId ORDER BY createdAt DESC")
    fun getWasteRecordsBySession(sessionId: Long): Flow<List<IngredientWasteRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWasteRecord(record: IngredientWasteRecordEntity): Long

    @Query("DELETE FROM ingredient_waste_records WHERE sessionId = :sessionId")
    suspend fun deleteWasteRecordsBySession(sessionId: Long)
}
