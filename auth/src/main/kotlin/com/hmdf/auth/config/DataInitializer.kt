package com.hmdf.auth.config

import com.hmdf.auth.domain.Member
import com.hmdf.auth.domain.Role
import javax.persistence.EntityManager
import javax.persistence.PersistenceContext
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Profile
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("local")
class DataInitializer(
    private val passwordEncoder: PasswordEncoder
) : ApplicationRunner {

    @PersistenceContext
    private lateinit var em: EntityManager

    @Transactional
    override fun run(args: ApplicationArguments) {
        em.persist(
            Member(
                username = "admin",
                password = passwordEncoder.encode("1"),
                userName = "관리자",
                empNo    = "313885",
                role     = Role.ADMIN
            )
        )
    }
}
