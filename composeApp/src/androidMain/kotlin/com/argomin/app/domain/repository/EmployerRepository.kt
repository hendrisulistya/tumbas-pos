package com.argomin.app.domain.repository

import com.argomin.app.data.local.entity.EmployerEntity
import kotlinx.coroutines.flow.Flow

interface EmployerRepository {
    fun getAll(): Flow<List<EmployerEntity>>
    suspend fun getAllSync(): List<EmployerEntity>
    suspend fun getById(id: Long): EmployerEntity?
    suspend fun insert(employer: EmployerEntity): Long
    suspend fun update(employer: EmployerEntity)
    suspend fun delete(employer: EmployerEntity)
    suspend fun initializeFromCsv()
}
