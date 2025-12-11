package com.argminres.app.data.repository

import android.content.Context
import com.argminres.app.data.local.dao.EmployerDao
import com.argminres.app.data.local.entity.EmployerEntity
import com.argminres.app.domain.repository.EmployerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.io.BufferedReader
import java.io.InputStreamReader

class EmployerRepositoryImpl(
    private val employerDao: EmployerDao,
    private val context: Context
) : EmployerRepository {
    
    override fun getAll(): Flow<List<EmployerEntity>> = employerDao.getAll()
    
    override suspend fun getById(id: Long): EmployerEntity? = employerDao.getById(id)
    
    override suspend fun insert(employer: EmployerEntity): Long {
        // Hash PIN before inserting
        val hashedEmployer = employer.copy(
            pin = com.argminres.app.util.PinHasher.hashPin(employer.pin)
        )
        return employerDao.insert(hashedEmployer)
    }
    
    override suspend fun update(employer: EmployerEntity) {
        // Hash PIN before updating
        val hashedEmployer = employer.copy(
            pin = com.argminres.app.util.PinHasher.hashPin(employer.pin)
        )
        employerDao.update(hashedEmployer)
    }
    
    override suspend fun delete(employer: EmployerEntity) = employerDao.delete(employer)
    
    override suspend fun initializeFromCsv() {
        // Check if employers already exist
        val existingEmployers = employerDao.getAll().first()
        if (existingEmployers.isNotEmpty()) {
            return // Already initialized
        }
        
        try {
            val inputStream = context.assets.open("employers.csv")
            val reader = BufferedReader(InputStreamReader(inputStream))
            
            // Skip header line
            reader.readLine()
            
            val employers = mutableListOf<EmployerEntity>()
            var line: String?
            
            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    val parts = it.split(",")
                    if (parts.size >= 4) {
                        val employer = EmployerEntity(
                            fullName = parts[0].trim(),
                            phoneNumber = parts[1].trim(),
                            role = parts[2].trim().uppercase(),
                            pin = com.argminres.app.util.PinHasher.hashPin(parts[3].trim()) // Hash PIN from CSV
                        )
                        employers.add(employer)
                    }
                }
            }
            
            reader.close()
            
            if (employers.isNotEmpty()) {
                employerDao.insertAll(employers)
                android.util.Log.d("EmployerRepository", "Initialized ${employers.size} employers from CSV")
            }
        } catch (e: Exception) {
            android.util.Log.e("EmployerRepository", "Error loading employers from CSV", e)
        }
    }
}
