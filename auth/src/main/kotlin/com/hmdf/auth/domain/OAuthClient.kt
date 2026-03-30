package com.hmdf.auth.domain

enum class OAuthClient(val clientId: String, val redirectUrls: Map<String, String>) {
    WEB_QUERY(
        clientId = "550e8400-e29b-41d4-a716-446655440000",
        redirectUrls = mapOf(
            Profile.LOCAL to "http://localhost:3000/callback",
            Profile.DEV   to "http://dev.web-query.yourdomain.com/callback",
            Profile.STAGE to "https://stage.web-query.yourdomain.com/callback",
            Profile.PROD  to "https://web-query.yourdomain.com/callback"
        )
    ),
    MASTER_MODEL(
        clientId = "550e8400-e29b-41d4-a716-446655440001",
        redirectUrls = mapOf(
            Profile.LOCAL to "http://localhost:3001/callback",
            Profile.DEV   to "http://dev.master-model.yourdomain.com/callback",
            Profile.STAGE to "https://stage.master-model.yourdomain.com/callback",
            Profile.PROD  to "https://master-model.yourdomain.com/callback"
        )
    ),
    LOCAL(
        clientId = "550e8400-e29b-41d4-a716-446655440002",
        redirectUrls = mapOf(
            Profile.LOCAL to "http://localhost:8080/callback",
            Profile.DEV   to "http://localhost:8080/callback",
            Profile.STAGE to "http://localhost:8080/callback",
            Profile.PROD  to "http://localhost:8080/callback"
        )
    );

    fun getRedirectUrl(profile: String): String =
        redirectUrls[profile] ?: redirectUrls[Profile.LOCAL]!!

    companion object {
        fun findByClientId(clientId: String): OAuthClient =
            values().find { it.clientId == clientId }
                ?: throw IllegalArgumentException(clientId)
    }
}
