package com.tumbaspos.app.domain.repository

import com.tumbaspos.app.data.local.dao.CustomerDao
import com.tumbaspos.app.data.local.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun getAllCustomers(): Flow<List<CustomerEntity>>
    suspend fun getCustomerById(id: Long): CustomerEntity?
    suspend fun insertCustomer(customer: CustomerEntity): Long
    suspend fun updateCustomer(customer: CustomerEntity)
    suspend fun deleteCustomer(customer: CustomerEntity)
}

class CustomerRepositoryImpl(
    private val customerDao: CustomerDao
) : CustomerRepository {
    override fun getAllCustomers(): Flow<List<CustomerEntity>> = customerDao.getAllCustomers()
    override suspend fun getCustomerById(id: Long): CustomerEntity? = customerDao.getCustomerById(id)
    override suspend fun insertCustomer(customer: CustomerEntity): Long = customerDao.insertCustomer(customer)
    override suspend fun updateCustomer(customer: CustomerEntity) = customerDao.updateCustomer(customer)
    override suspend fun deleteCustomer(customer: CustomerEntity) = customerDao.deleteCustomer(customer)
}
