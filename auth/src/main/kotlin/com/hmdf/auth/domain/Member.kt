package com.hmdf.auth.domain

import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name = "member")
class Member(

    @Column(nullable = false, unique = true, length = 50)
    val username: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false, length = 100)
    var userName: String,

    @Column(nullable = false, unique = true, length = 20)
    var empNo: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: Role = Role.USER,

    @Column(nullable = false)
    var enabled: Boolean = true,

    @Column(nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0
)

enum class Role {
    USER, ADMIN
}
