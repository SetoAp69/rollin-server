package com.rollinup.server.model.auth

data class UserCreateEditRequest(
    val id: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val address: String = "",
    val email: String = "",
    val gender: String = "",
    val password: String = "",
)
