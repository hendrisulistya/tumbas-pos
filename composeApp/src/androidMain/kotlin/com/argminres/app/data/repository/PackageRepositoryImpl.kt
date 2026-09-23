package com.argminres.app.data.repository

import com.argminres.app.data.local.dao.PackageDao
import com.argminres.app.data.local.entity.PackageEntity
import com.argminres.app.domain.repository.PackageRepository
import kotlinx.coroutines.flow.Flow

class PackageRepositoryImpl(
    private val packageDao: PackageDao
) : PackageRepository {
    override fun getAllPackages(): Flow<List<PackageEntity>> {
        return packageDao.getAllPackages()
    }

    override suspend fun getPackageById(id: Long): PackageEntity? {
        return packageDao.getPackageById(id)
    }

    override fun searchPackages(query: String): Flow<List<PackageEntity>> {
        return packageDao.searchPackages(query)
    }

    override suspend fun insertPackage(pkg: PackageEntity): Long {
        return packageDao.insertPackage(pkg)
    }

    override suspend fun updatePackage(pkg: PackageEntity) {
        packageDao.updatePackage(pkg)
    }

    override suspend fun deletePackage(pkg: PackageEntity) {
        packageDao.deletePackage(pkg)
    }
}
