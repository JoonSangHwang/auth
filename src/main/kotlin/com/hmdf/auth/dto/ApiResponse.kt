package com.hmdf.auth.dto

import com.fasterxml.jackson.annotation.JsonInclude

data class ApiResponse<T>(
    val success: Boolean,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val data: T? = null,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val code: String? = null,
    val message: String
) {
    companion object {
        fun <T> ok(data: T, message: String = "성공"): ApiResponse<T> =
            ApiResponse(success = true, data = data, message = message)

        fun ok(message: String = "성공"): ApiResponse<Unit> =
            ApiResponse(success = true, message = message)

        fun error(code: String, message: String): ApiResponse<Nothing> =
            ApiResponse(success = false, code = code, message = message)
    }
}
