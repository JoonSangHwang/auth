package com.hmdf.auth.interceptor

import com.hmdf.auth.exception.AuthErrorCode
import com.hmdf.auth.exception.AuthException
import com.hmdf.auth.util.JwtUtil
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class TokenValidationInterceptor(private val jwtUtil: JwtUtil) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        // HandlerMethod 가 아니면 (e.g. 정적 리소스) 그냥 통과
        if (handler !is HandlerMethod) return true

        // @RequireAuth 없으면 통과
        if (!handler.hasMethodAnnotation(RequireAuth::class.java)) return true

        val token = extractTokenFromCookie(request)
            ?: throw AuthException(AuthErrorCode.MISSING_TOKEN)

        if (!jwtUtil.validateToken(token)) {
            throw AuthException(AuthErrorCode.INVALID_TOKEN)
        }

        request.setAttribute("username", jwtUtil.getUsername(token))
        return true
    }

    private fun extractTokenFromCookie(request: HttpServletRequest): String? {
        return request.cookies?.find { it.name == "access_token" }?.value
    }
}
