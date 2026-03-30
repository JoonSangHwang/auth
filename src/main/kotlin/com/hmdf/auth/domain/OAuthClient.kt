package com.hmdf.auth.domain

enum class OAuthClient(val clientId: String, val allowedRedirectUris: List<String>) {
    WEB_QUERY(
        clientId = "550e8400-e29b-41d4-a716-446655440000",
        allowedRedirectUris = listOf(
            "http://localhost:3000/callback",
            "http://dev.web-query.yourdomain.com/callback",
            "https://stage.web-query.yourdomain.com/callback",
            "https://web-query.yourdomain.com/callback"
        )
    ),
    MASTER_MODEL(
        clientId = "550e8400-e29b-41d4-a716-446655440001",
        allowedRedirectUris = listOf(
            "http://localhost:3001/callback",
            "http://dev.master-model.yourdomain.com/callback",
            "https://stage.master-model.yourdomain.com/callback",
            "https://master-model.yourdomain.com/callback"
        )
    ),
    LOCAL(
        clientId = "550e8400-e29b-41d4-a716-446655440002",
        allowedRedirectUris = listOf(
            "http://localhost:8080/callback"
        )
    );

    fun isValidRedirectUri(uri: String): Boolean = allowedRedirectUris.contains(uri)

    companion object {
        fun findByClientId(clientId: String): OAuthClient =
            values().find { it.clientId == clientId }
                ?: throw IllegalArgumentException(clientId)
    }
}
