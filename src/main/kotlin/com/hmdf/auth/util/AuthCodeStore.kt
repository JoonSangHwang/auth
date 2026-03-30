package com.hmdf.auth.util

import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class AuthCodeInfo(
    val clientId: String,
    val redirectUri: String,
    val username: String,
    val expiresAt: Instant
)

@Component
class AuthCodeStore {
    private val store = ConcurrentHashMap<String, AuthCodeInfo>()

    fun issue(clientId: String, redirectUri: String, username: String): String {
        val code = UUID.randomUUID().toString().replace("-", "")
        store[code] = AuthCodeInfo(
            clientId = clientId,
            redirectUri = redirectUri,
            username = username,
            expiresAt = Instant.now().plusSeconds(300)
        )
        return code
    }

    fun consume(code: String): AuthCodeInfo? {
        val info = store.remove(code) ?: return null
        if (Instant.now().isAfter(info.expiresAt)) return null
        return info
    }
}
