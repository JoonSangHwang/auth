package com.hmdf.auth.exception

enum class AuthErrorCode(val code: String, val message: String) {
    INVALID_CREDENTIALS      ("AUTH-001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    MISSING_TOKEN            ("AUTH-002", "토큰이 없습니다."),
    INVALID_TOKEN            ("AUTH-003", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN            ("AUTH-004", "만료된 토큰입니다."),
    INVALID_CLIENT           ("AUTH-005", "등록되지 않은 클라이언트입니다."),
    INVALID_REDIRECT_URI     ("AUTH-006", "등록되지 않은 redirect_uri입니다."),
    INVALID_AUTH_CODE        ("AUTH-007", "유효하지 않은 인가 코드입니다."),
    UNSUPPORTED_RESPONSE_TYPE("AUTH-008", "지원하지 않는 response_type입니다."),
    UNSUPPORTED_GRANT_TYPE   ("AUTH-009", "지원하지 않는 grant_type입니다.")
}

class AuthException(val errorCode: AuthErrorCode) : RuntimeException(errorCode.message)
