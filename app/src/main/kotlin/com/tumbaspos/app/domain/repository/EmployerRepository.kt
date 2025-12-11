package com.tumbaspos.app.domain.repository

import com.tumbaspos.app.data.local.entity.EmployerEntity
import kotlinx.coroutines.flow.Flow

interface EmployerRepository {
    fun getAll(): Flow<List<EmployerEntity>>
    suspend fun getById(id: Long): EmployerEntity?
    suspend fun insert(employer: EmployerEntity): Long
    suspend fun update(employer: EmployerEntity)
    suspend fun delete(employer: EmployerEntity)
    suspend fun initializeFromCsv()
}
