package com.hmdf.auth

import com.hmdf.auth.domain.Profile
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AuthApplication

fun main(args: Array<String>) {
    runApplication<AuthApplication>(*args) {
        setAdditionalProfiles(*activeProfiles(args))
    }
}

private fun activeProfiles(args: Array<String>): Array<String> {
    val hasProfile = args.any { it.startsWith("--spring.profiles.active") }
        || System.getenv("SPRING_PROFILES_ACTIVE") != null
        || System.getProperty("spring.profiles.active") != null
    return if (hasProfile) emptyArray() else arrayOf(Profile.LOCAL)
}
