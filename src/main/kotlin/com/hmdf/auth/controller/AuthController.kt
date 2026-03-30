package com.hmdf.auth.controller

import com.hmdf.auth.dto.ApiResponse
import com.hmdf.auth.dto.TokenRequest
import com.hmdf.auth.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseBody
import javax.servlet.http.HttpServletResponse

@Tag(name = "Auth", description = "인증 API")
@Controller
class AuthController(private val authService: AuthService) {

    @Operation(summary = "인가 요청", description = "clientId·redirectUri·responseType 검증 후 /login 으로 리다이렉트")
    @GetMapping("/authorize")
    fun authorize(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam("response_type") responseType: String
    ): String {
        authService.validateAuthorize(clientId, redirectUri, responseType)
        return "redirect:/login?client_id=$clientId&redirect_uri=${java.net.URLEncoder.encode(redirectUri, "UTF-8")}"
    }

    @Operation(summary = "로그인 페이지", description = "로그인 화면 반환")
    @GetMapping("/login")
    fun loginPage(
        @RequestParam("client_id") clientId: String,
        @RequestParam("redirect_uri") redirectUri: String,
        @RequestParam(required = false) error: String?,
        model: Model
    ): String {
        model.addAttribute("clientId", clientId)
        model.addAttribute("redirectUri", redirectUri)
        if (error != null) model.addAttribute("error", error)
        return "login"
    }

    @Operation(summary = "로그인 처리", description = "자격증명 검증 후 인가 코드 발급, redirect_uri?code=xxx 로 리다이렉트")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "302", description = "인가 코드 발급 — redirect_uri?code=xxx 로 리다이렉트"),
        SwaggerApiResponse(responseCode = "401", description = "아이디 또는 비밀번호 불일치"),
        SwaggerApiResponse(responseCode = "400", description = "등록되지 않은 클라이언트 또는 redirect_uri")
    )
    @PostMapping("/login")
    fun loginPost(
        @RequestParam clientId: String,
        @RequestParam redirectUri: String,
        @RequestParam username: String,
        @RequestParam password: String
    ): String {
        return try {
            val redirectTo = authService.authorize(clientId, redirectUri, username, password)
            "redirect:$redirectTo"
        } catch (e: com.hmdf.auth.exception.AuthException) {
            "redirect:/login?client_id=$clientId&redirect_uri=${java.net.URLEncoder.encode(redirectUri, "UTF-8")}&error=${java.net.URLEncoder.encode(e.errorCode.message, "UTF-8")}"
        }
    }

    @Operation(summary = "토큰 발급", description = "인가 코드로 AT·RT 쿠키 발급")
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "토큰 발급 성공 — access_token, refresh_token 쿠키 Set"),
        SwaggerApiResponse(responseCode = "400", description = "유효하지 않은 인가 코드 또는 grant_type")
    )
    @PostMapping("/oauth/token")
    @ResponseBody
    fun token(
        @RequestBody request: TokenRequest,
        response: HttpServletResponse
    ): ResponseEntity<ApiResponse<Unit>> {
        authService.token(request.grantType, request.code, request.clientId, request.redirectUri, response)
        return ResponseEntity.ok(ApiResponse.ok("토큰이 발급되었습니다."))
    }

    @Operation(summary = "토큰 폐기", description = "AT·RT 쿠키 삭제")
    @SwaggerApiResponse(responseCode = "200", description = "토큰 폐기 성공")
    @PostMapping("/oauth/revoke")
    @ResponseBody
    fun revoke(response: HttpServletResponse): ResponseEntity<ApiResponse<Unit>> {
        authService.logout(response)
        return ResponseEntity.ok(ApiResponse.ok("로그아웃이 완료되었습니다."))
    }
}
