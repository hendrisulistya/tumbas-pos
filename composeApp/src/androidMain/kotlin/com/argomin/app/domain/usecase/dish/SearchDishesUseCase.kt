package com.argomin.app.domain.usecase.dish

import com.argomin.app.domain.repository.DishRepository
import kotlinx.coroutines.flow.Flow

class SearchDishesUseCase(
    private val dishRepository: DishRepository
) {
    operator fun invoke(query: String): Flow<List<com.argomin.app.data.local.dao.DishWithCategory>> {
        return dishRepository.searchDishes(query)
    }
}
