package com.argminres.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employerId: Long,
    val employerName: String,
    val action: String,        // LOGIN, LOGOUT, CREATE, UPDATE, DELETE
    val entityType: String,    // SALES_ORDER, PURCHASE_ORDER, PRODUCT, EMPLOYEE
    val entityId: Long?,       // ID of affected entity
    val details: String?,      // Additional context
    val timestamp: Long = System.currentTimeMillis()
)
