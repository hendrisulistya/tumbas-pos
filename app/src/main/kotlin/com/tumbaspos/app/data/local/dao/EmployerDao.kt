package com.tumbaspos.app.data.local.dao

import androidx.room.*
import com.tumbaspos.app.data.local.entity.EmployerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployerDao {
    @Query("SELECT * FROM employers")
    fun getAll(): Flow<List<EmployerEntity>>
    
    @Query("SELECT * FROM employers WHERE id = :id")
    suspend fun getById(id: Long): EmployerEntity?
    
    @Query("SELECT * FROM employers WHERE pin = :pin LIMIT 1")
    suspend fun getByPin(pin: String): EmployerEntity?
    
    @Query("SELECT * FROM employers WHERE role = :role")
    fun getByRole(role: String): Flow<List<EmployerEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employer: EmployerEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employers: List<EmployerEntity>)
    
    @Update
    suspend fun update(employer: EmployerEntity)
    
    @Delete
    suspend fun delete(employer: EmployerEntity)
    
    @Query("DELETE FROM employers")
    suspend fun deleteAll()
}
