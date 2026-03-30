package com.hmdf.auth.service

import com.hmdf.auth.domain.OAuthClient
import com.hmdf.auth.domain.Profile
import com.hmdf.auth.exception.AuthErrorCode
import com.hmdf.auth.exception.AuthException
import com.hmdf.auth.repository.MemberRepository
import com.hmdf.auth.util.JwtUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse

@Service
class AuthService(
    private val jwtUtil: JwtUtil,
    private val passwordEncoder: PasswordEncoder,
    private val memberRepository: MemberRepository,
    private val environment: Environment,
    @Value("\${cookie.domain:}") private val cookieDomain: String
) {
    private val activeProfile: String
        get() = environment.activeProfiles.firstOrNull() ?: Profile.LOCAL

    fun login(clientId: String, username: String, password: String, response: HttpServletResponse): String {
        val client = try {
            OAuthClient.findByClientId(clientId)
        } catch (e: IllegalArgumentException) {
            throw AuthException(AuthErrorCode.INVALID_CLIENT)
        }

        val member = memberRepository.findByUsername(username)
            .orElseThrow { AuthException(AuthErrorCode.INVALID_CREDENTIALS) }

        if (!passwordEncoder.matches(password, member.password)) {
            throw AuthException(AuthErrorCode.INVALID_CREDENTIALS)
        }

        val accessToken = jwtUtil.generateAccessToken(username)
        val refreshToken = jwtUtil.generateRefreshToken(username)

        val atMaxAge = ChronoUnit.SECONDS.between(
            LocalDateTime.now(),
            LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 59, 59))
        ).toInt()

        setCookie(response, "access_token", accessToken, atMaxAge)
        setCookie(response, "refresh_token", refreshToken, 7 * 24 * 60 * 60)

        return client.getRedirectUrl(activeProfile)
    }

    fun logout(response: HttpServletResponse) {
        setCookie(response, "access_token", "", 0)
        setCookie(response, "refresh_token", "", 0)
    }

    private fun setCookie(response: HttpServletResponse, name: String, value: String, maxAge: Int) {
        val cookie = Cookie(name, value).apply {
            isHttpOnly = true
            path = "/"
            if (cookieDomain.isNotBlank()) {
                domain = cookieDomain
            }
            this.maxAge = maxAge
        }
        response.addCookie(cookie)
    }
}
