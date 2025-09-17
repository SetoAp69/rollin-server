package com.rollinup.server.datasource.database.model.user

data class UserDTO(
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val roles: String = "",
    val assignedClass: Class = Class(),
    val gender: String = "",

    /*Token*/
    val accessToken: String = "",
    val refreshToken: String = "",
) {
    val fullName
        get() = "$firstName $lastName"

    data class Class(
        val id: String = "",
        val grade: String = "",
        val department: String = "",
        val name: String = "",
    )
}
