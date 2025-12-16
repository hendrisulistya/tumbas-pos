package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.DishComponentDao
import com.argminres.app.data.local.dao.DishDao
import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.data.local.entity.DishComponentEntity
import com.argminres.app.domain.repository.DishComponentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class DishComponentRepositoryImpl(
    private val dishComponentDao: DishComponentDao,
    private val dishDao: DishDao
) : DishComponentRepository {
    
    override fun getComponentsForPackage(packageId: Long): Flow<List<DishWithCategory>> {
        return dishComponentDao.getComponentDishIds(packageId).map { dishIds ->
            dishIds.mapNotNull { dishId ->
                dishDao.getDishById(dishId)
            }
        }
    }
    
    override fun getComponentEntities(packageId: Long): Flow<List<DishComponentEntity>> {
        return dishComponentDao.getComponentEntities(packageId)
    }
    
    override suspend fun addComponent(packageId: Long, componentId: Long) {
        val component = DishComponentEntity(
            packageDishId = packageId,
            componentDishId = componentId,
            quantity = 1
        )
        dishComponentDao.insertComponent(component)
    }
    
    override suspend fun removeComponent(componentId: Long) {
        dishComponentDao.deleteComponent(componentId)
    }
    
    override suspend fun removeAllComponents(packageId: Long) {
        dishComponentDao.deleteAllComponentsForPackage(packageId)
    }
}
