package com.argminres.app.domain.usecase.dish

import com.argminres.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.Flow

class SearchDishesUseCase(
    private val dishRepository: DishRepository
) {
    operator fun invoke(query: String): Flow<List<com.argminres.app.data.local.dao.DishWithCategory>> {
        return dishRepository.searchDishes(query)
    }
}
