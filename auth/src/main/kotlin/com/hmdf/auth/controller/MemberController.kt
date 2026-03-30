package com.hmdf.auth.controller

import com.hmdf.auth.dto.ApiResponse
import com.hmdf.auth.dto.MemberResponse
import com.hmdf.auth.interceptor.RequireAuth
import com.hmdf.auth.service.MemberService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Member", description = "회원 API")
@RestController
@RequestMapping("/oauth")
class MemberController(private val memberService: MemberService) {

    @Operation(
        summary = "사용자 정보 조회",
        description = "AT 쿠키를 인터셉터에서 검증 후 DB에서 사용자 정보 반환",
        security = [SecurityRequirement(name = "access_token (cookie)")]
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "조회 성공"),
        SwaggerApiResponse(responseCode = "401", description = "토큰 없음 또는 유효하지 않음")
    )
    @RequireAuth
    @GetMapping("/userinfo")
    fun userinfo(@RequestAttribute username: String): ResponseEntity<ApiResponse<MemberResponse>> {
        return ResponseEntity.ok(ApiResponse.ok(memberService.getMyInfo(username), "사용자 정보를 조회하였습니다."))
    }
}
