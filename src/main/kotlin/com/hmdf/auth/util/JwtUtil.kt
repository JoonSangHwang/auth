package com.hmdf.auth.util

import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date

@Component
class JwtUtil(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.refresh-expiry-days:7}") private val refreshExpiryDays: Long
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    // AT: 발급 시점 무관, 당일 23:59:59 만료
    fun generateAccessToken(username: String): String {
        val expiry = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .let { Date.from(it) }

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // RT: 설정된 일수만큼 유효
    fun generateRefreshToken(username: String): String {
        val expiry = Date(System.currentTimeMillis() + refreshExpiryDays * 24 * 60 * 60 * 1000)

        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(Date())
            .setExpiration(expiry)
            .signWith(key, SignatureAlgorithm.HS256)
            .compact()
    }

    // 시그니처 + 만료 시간만 검증
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
            true
        } catch (e: JwtException) {
            false
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun getUsername(token: String): String = getClaims(token).subject

    private fun getClaims(token: String): Claims {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .body
    }
}
