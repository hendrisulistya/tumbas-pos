package com.argminres.app.domain.usecase.ingredient

import com.argminres.app.data.local.entity.IngredientEntity
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

class SearchIngredientsUseCase(
    private val ingredientRepository: IngredientRepository
) {
    operator fun invoke(query: String): Flow<List<IngredientEntity>> {
        return ingredientRepository.searchIngredients(query)
    }
}

class GetIngredientsUseCase(
    private val ingredientRepository: IngredientRepository
) {
    operator fun invoke(): Flow<List<IngredientEntity>> {
        return ingredientRepository.getAllIngredients()
    }
}
