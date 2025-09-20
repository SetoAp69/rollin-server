package com.rollinup.server.datasource.database.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("_id")
    val id: String = "",
    @SerialName("username")
    val userName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("firstName")
    val firstName: String = "",
    @SerialName("lastName")
    val lastName: String = "",
    @SerialName("roles")
    val roles: String = "",
    @SerialName("class")
    val assignedClass: Class = Class(),
    @SerialName("gender")
    val gender: String = "",

    /*Token*/
    @SerialName("accessToken")
    val accessToken: String = "",
    @SerialName("refreshToken")
    val refreshToken: String = "",
) {
    val fullName
        get() = "$firstName $lastName"

    @Serializable
    data class Class(
        @SerialName("_id")
        val id: String = "",
        @SerialName("grade")
        val grade: String = "",
        @SerialName("department")
        val department: String = "",
        @SerialName("name")
        val name: String = "",
    )
}
