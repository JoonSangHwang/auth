package com.hmdf.auth.dto

data class LoginRequest(
    val clientId: String,
    val username: String,
    val password: String
)
