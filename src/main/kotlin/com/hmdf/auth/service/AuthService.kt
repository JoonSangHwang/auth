package com.hmdf.auth.service

import com.hmdf.auth.domain.OAuthClient
import com.hmdf.auth.exception.AuthErrorCode
import com.hmdf.auth.exception.AuthException
import com.hmdf.auth.repository.MemberRepository
import com.hmdf.auth.util.AuthCodeStore
import com.hmdf.auth.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseCookie
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.servlet.http.HttpServletResponse

@Service
class AuthService(
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
    private val authCodeStore: AuthCodeStore,
    @Value("\${cookie.domain:}") private val cookieDomain: String
) {
    companion object {
        private const val SUPPORTED_RESPONSE_TYPE = "code"
        private const val SUPPORTED_GRANT_TYPE = "authorization_code"
    }

    fun validateAuthorize(clientId: String, redirectUri: String, responseType: String) {
        if (responseType != SUPPORTED_RESPONSE_TYPE) {
            throw AuthException(AuthErrorCode.UNSUPPORTED_RESPONSE_TYPE)
        }
        val client = findClient(clientId)
        if (!client.isValidRedirectUri(redirectUri)) {
            throw AuthException(AuthErrorCode.INVALID_REDIRECT_URI)
        }
    }

    fun authorize(clientId: String, redirectUri: String, username: String, password: String): String {
        val client = findClient(clientId)
        if (!client.isValidRedirectUri(redirectUri)) {
            throw AuthException(AuthErrorCode.INVALID_REDIRECT_URI)
        }

        val member = memberRepository.findByUsername(username)
            .orElseThrow { AuthException(AuthErrorCode.INVALID_CREDENTIALS) }

        if (!passwordEncoder.matches(password, member.password)) {
            throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)
        }

        val code = authCodeStore.issue(clientId, redirectUri, username)
        return "$redirectUri?code=$code"
    }

    fun token(grantType: String, code: String, clientId: String, redirectUri: String, response: HttpServletResponse) {
        if (grantType != SUPPORTED_GRANT_TYPE) {
            throw AuthException(AuthErrorCode.UNSUPPORTED_GRANT_TYPE)
        }

        val info = authCodeStore.consume(code)
            ?: throw AuthException(AuthErrorCode.INVALID_AUTH_CODE)

        if (info.clientId != clientId || info.redirectUri != redirectUri) {
            throw AuthException(AuthErrorCode.INVALID_AUTH_CODE)
        }

        val accessToken = jwtUtil.generateAccessToken(info.username)
        val refreshToken = jwtUtil.generateRefreshToken(info.username)

        val atMaxAge = ChronoUnit.SECONDS.between(
            LocalDateTime.now(),
            LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
        )

        setCookie(response, "access_token", accessToken, atMaxAge)
        setCookie(response, "refresh_token", refreshToken, 7 * 24 * 60 * 60)
    }

    fun logout(response: HttpServletResponse) {
        setCookie(response, "access_token", "", 0)
        setCookie(response, "refresh_token", "", 0)
    }

    private fun findClient(clientId: String): OAuthClient =
        try {
            OAuthClient.findByClientId(clientId)
        } catch (e: IllegalArgumentException) {
            throw AuthException(AuthErrorCode.INVALID_CLIENT)
        }

    private fun setCookie(response: HttpServletResponse, name: String, value: String, maxAge: Long) {
        val cookie = ResponseCookie.from(name, value)
            .httpOnly(true)
            .path("/")
            .sameSite("Lax")
            .maxAge(Duration.ofSeconds(maxAge))
            .apply { if (cookieDomain.isNotBlank()) domain(cookieDomain) }
            .build()

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString())
    }
}
