package com.argomin.app.domain.model

data class Employee(
    val id: String,
    val name: String,
    val role: String,
    val phone: String,
    val status: String,
    val pin: String = "1234"
)
