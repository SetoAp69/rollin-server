package com.rollinup.server.model.response.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    @SerialName("id")
    val id: String = "",
    @SerialName("user_name")
    val userName: String = "",
    @SerialName("studentId")
    val studentId: String? = null,
    @SerialName("email")
    val email: String = "",
    @SerialName("first_name")
    val firstName: String = "",
    @SerialName("last_name")
    val lastName: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("address")
    val address:String = "",
    @SerialName("gender")
    val gender: String = "",
    @SerialName("class")
    val classX: String? = null,
    @SerialName("classId")
    val classId: String? = null,
    @SerialName("classKey")
    val classKey: Int? = null,
    @SerialName("isVerified")
    val isVerified: Boolean = false,
    @SerialName("deviceId")
    val deviceId:String? = null
)