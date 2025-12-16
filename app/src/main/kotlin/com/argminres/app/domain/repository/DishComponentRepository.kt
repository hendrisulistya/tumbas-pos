package com.argminres.app.domain.repository

import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.DishComponentEntity
import kotlinx.coroutines.flow.Flow

interface DishComponentRepository {
    fun getComponentsForPackage(packageId: Long): Flow<List<DishWithCategory>>
    fun getComponentEntities(packageId: Long): Flow<List<DishComponentEntity>>
    suspend fun addComponent(packageId: Long, componentId: Long)
    suspend fun removeComponent(componentId: Long)
    suspend fun removeAllComponents(packageId: Long)
}
