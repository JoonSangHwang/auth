package com.hmdf.auth.service

import com.hmdf.auth.dto.MemberResponse
import com.hmdf.auth.exception.AuthErrorCode
import com.hmdf.auth.exception.AuthException
import com.hmdf.auth.repository.MemberRepository
import org.springframework.stereotype.Service

@Service
class MemberService(private val memberRepository: MemberRepository) {

    fun getMyInfo(username: String): MemberResponse {
        val member = memberRepository.findByUsername(username)
            .orElseThrow { AuthException(AuthErrorCode.INVALID_CREDENTIALS) }

        return MemberResponse(
            userId   = member.username,
            userName = member.userName,
            empNo    = member.empNo
        )
    }
}
