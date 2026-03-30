package com.hmdf.auth.dto

data class TokenRequest(
    val grantType: String,
    val code: String,
    val clientId: String,
    val redirectUri: String
)
