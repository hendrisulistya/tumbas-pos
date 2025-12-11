package com.argminres.app.domain.usecase.ingredient

import com.argminres.app.data.local.dao.IngredientWithCategory
import com.argminres.app.domain.repository.IngredientRepository
import kotlinx.coroutines.flow.Flow

class SearchIngredientsUseCase(
    private val ingredientRepository: IngredientRepository
) {
    operator fun invoke(query: String): Flow<List<IngredientWithCategory>> {
        return ingredientRepository.searchIngredients(query)
    }
}

class GetIngredientsUseCase(
    private val ingredientRepository: IngredientRepository
) {
    operator fun invoke(): Flow<List<IngredientWithCategory>> {
        return ingredientRepository.getAllIngredients()
    }
}
