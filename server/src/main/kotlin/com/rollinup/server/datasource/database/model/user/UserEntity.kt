package com.rollinup.server.datasource.database.model.user

import com.rollinup.server.model.Role

data class UserEntity(
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: Role = Role.STUDENT,
    val gender: String = "",
    val password: String = "",
    val salt: String = "",
    val device: String? = null,
)
