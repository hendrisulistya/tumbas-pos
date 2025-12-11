package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.ContactDao
import com.argminres.app.data.local.dao.StockDao
import com.argminres.app.data.local.entity.StockMovementEntity
import com.argminres.app.data.local.entity.SupplierEntity
import com.argminres.app.domain.repository.StockRepository
import com.argminres.app.domain.repository.SupplierRepository
import kotlinx.coroutines.flow.Flow

class StockRepositoryImpl(
    private val stockDao: StockDao
) : StockRepository {
    override fun getStockMovementsForDish(productId: Long): Flow<List<StockMovementEntity>> {
        return stockDao.getStockMovementsForDish(productId)
    }

    override suspend fun insertStockMovement(movement: StockMovementEntity) {
        stockDao.insertStockMovement(movement)
    }
}

class SupplierRepositoryImpl(
    private val contactDao: ContactDao
) : SupplierRepository {
    override fun getAllSuppliers(): Flow<List<SupplierEntity>> {
        return contactDao.getAllSuppliers()
    }

    override suspend fun getSupplierById(id: Long): SupplierEntity? {
        return contactDao.getSupplierById(id)
    }

    override suspend fun insertSupplier(supplier: SupplierEntity): Long {
        return contactDao.insertSupplier(supplier)
    }

    override suspend fun updateSupplier(supplier: SupplierEntity) {
        contactDao.updateSupplier(supplier)
    }

    override suspend fun deleteSupplier(supplier: SupplierEntity) {
        contactDao.deleteSupplier(supplier)
    }
}
