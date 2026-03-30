package com.hmdf.auth.exception

import com.hmdf.auth.dto.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AuthException::class)
    fun handleAuthException(e: AuthException): ResponseEntity<ApiResponse<Nothing>> {
        val status = when (e.errorCode) {
            AuthErrorCode.INVALID_CREDENTIALS       -> HttpStatus.UNAUTHORIZED
            AuthErrorCode.MISSING_TOKEN             -> HttpStatus.UNAUTHORIZED
            AuthErrorCode.INVALID_TOKEN             -> HttpStatus.UNAUTHORIZED
            AuthErrorCode.EXPIRED_TOKEN             -> HttpStatus.UNAUTHORIZED
            AuthErrorCode.INVALID_CLIENT            -> HttpStatus.BAD_REQUEST
            AuthErrorCode.INVALID_REDIRECT_URI      -> HttpStatus.BAD_REQUEST
            AuthErrorCode.INVALID_AUTH_CODE         -> HttpStatus.BAD_REQUEST
            AuthErrorCode.UNSUPPORTED_RESPONSE_TYPE -> HttpStatus.BAD_REQUEST
            AuthErrorCode.UNSUPPORTED_GRANT_TYPE    -> HttpStatus.BAD_REQUEST
        }
        return ResponseEntity.status(status).body(
            ApiResponse.error(code = e.errorCode.code, message = e.errorCode.message)
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ApiResponse.error(code = "SERVER-001", message = "서버 오류가 발생했습니다.")
        )
    }
}
