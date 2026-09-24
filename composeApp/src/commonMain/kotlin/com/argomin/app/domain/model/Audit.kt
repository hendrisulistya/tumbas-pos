package com.argomin.app.domain.model

data class AuditEntry(
    val time: String,
    val user: String,
    val action: String,
    val details: String
)
