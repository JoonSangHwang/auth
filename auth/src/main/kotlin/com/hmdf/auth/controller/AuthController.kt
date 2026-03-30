package com.hmdf.auth.controller

import com.hmdf.auth.dto.ApiResponse
import com.hmdf.auth.dto.LoginRequest
import com.hmdf.auth.dto.TokenResponse
import com.hmdf.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletResponse

@Tag(name = "Auth", description = "인증 API")
@Controller
class AuthController(private val authService: AuthService) {

    @Operation(summary = "로그인 페이지", description = "Thymeleaf 로그인 화면 반환")
    @GetMapping("/login")
    fun loginPage(): String = "login"

    @Operation(summary = "토큰 발급", description = "clientId 검증 후 AT·RT 쿠키 발급, 클라이언트 redirectUrl 반환")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "토큰 발급 성공 — access_token, refresh_token 쿠키 Set"),
        SwaggerApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
        SwaggerApiResponse(responseCode = "401", description = "등록되지 않은 클라이언트")
    )
    @PostMapping("/oauth/token")
    @ResponseBody
    fun token(
        @RequestBody request: LoginRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<TokenResponse>> {
        val redirectUrl = authService.login(request.clientId, request.username, request.password, response)
        return ResponseEntity.ok(ApiResponse.ok(TokenResponse(redirectUrl = redirectUrl)))
    }

    @Operation(summary = "토큰 폐기", description = "AT·RT 쿠키 삭제")
    @SwaggerApiResponse(responseCode = "200", description = "토큰 폐기 성공")
    @PostMapping("/oauth/revoke")
    @ResponseBody
    fun revoke(response: HttpServletResponse): ResponseEntity<ApiResponse<Unit>> {
        authService.logout(response)
        return ResponseEntity.ok(ApiResponse.ok())
    }
}
