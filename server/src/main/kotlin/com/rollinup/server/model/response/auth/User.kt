package com.rollinup.server.model.response.auth

import com.rollinup.server.datasource.database.model.user.UserDTO

data class User(
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val role: String = "",
    val gender: String = "",
    val password: String = "",
    val salt: String = "",
){
    fun toDTO() = UserDTO(
        id = id,
        userName = userName,
        email = email,
        firstName = firstName,
        lastName = lastName,
        roles = role,
//        assignedClass = ,
        gender = gender,
    )
}