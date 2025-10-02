package com.rollinup.server.model.response.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_name")
    val userName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("gender")
    val gender: String = "",
)