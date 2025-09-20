package com.rollinup.server.model.request.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterEditUserRequest(
    @SerialName("userName")
    val userName: String = "",
    @SerialName("firstName")
    val firstName: String = "",
    @SerialName("lastName")
    val lastName: String = "",
    @SerialName("email")
    val email: String = "",
    @SerialName("password")
    val password: String = "",
    @SerialName("role")
    val role: String = "",
    @SerialName("address")
    val address: String = "",
    @SerialName("assignedClass")
    val assignedClass: String = "",
    @SerialName("phoneNumber")
    val phoneNumber: String = "",
    @SerialName("gender")
    val gender: String = "",
    val salt: String = ""

) {
    val isInvalid
        get() = listOf(
            userName,
            firstName,
            lastName,
            email,
            password,
            role,
            assignedClass
        ).any { it.isBlank() }
}
