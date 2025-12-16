package com.argminres.app.domain.usecase.recipe

import com.argminres.app.data.local.dao.DishWithCategory
import com.argminres.app.domain.repository.DishComponentRepository
import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Get all components for a package dish
 */
class GetPackageComponentsUseCase(
    private val componentRepository: DishComponentRepository
) {
    operator fun invoke(packageId: Long): Flow<List<DishWithCategory>> {
        return componentRepository.getComponentsForPackage(packageId)
    }
}

/**
 * Check if a package is available based on component stock
 */
class GetPackageAvailabilityUseCase(
    private val componentRepository: DishComponentRepository
) {
    /**
     * Check if package can be made (all components have stock > 0)
     */
    suspend fun isAvailable(packageId: Long): Boolean {
        val components = componentRepository.getComponentsForPackage(packageId).first()
        if (components.isEmpty()) return true // No components = always available
        return components.all { it.dish.stock > 0 }
    }
    
    /**
     * Get minimum available quantity based on component stocks
     */
    suspend fun getMinimumAvailableQuantity(packageId: Long): Int {
        val components = componentRepository.getComponentsForPackage(packageId).first()
        if (components.isEmpty()) return 999 // No components = unlimited
        return components.minOfOrNull { it.dish.stock } ?: 0
    }
}

/**
 * Manage package recipes (add/remove components)
 */
class ManageRecipeUseCase(
    private val componentRepository: DishComponentRepository
) {
    suspend fun addComponent(packageId: Long, componentId: Long) {
        componentRepository.addComponent(packageId, componentId)
    }
    
    suspend fun removeComponent(componentId: Long) {
        componentRepository.removeComponent(componentId)
    }
    
    suspend fun removeAllComponents(packageId: Long) {
        componentRepository.removeAllComponents(packageId)
    }
}

/**
 * Get all package dishes (category = Paket, categoryId = 4)
 */
class GetPackageDishesUseCase(
    private val dishRepository: DishRepository
) {
    operator fun invoke(): Flow<List<DishWithCategory>> {
        return dishRepository.searchDishes("") // Get all, filter in UI
    }
}
