package com.rollinup.server.model.request.auth

import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName("username")
    val username: String = "",
    @SerialName("password")
    val password: String = "",
) {
    private object ValidationMessages {
        const val USERNAME_BLANK = "Username cannot be empty."
        const val PASSWORD_BLANK = "Password cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            username.isBlank() -> ValidationResult.Invalid(ValidationMessages.USERNAME_BLANK)
            password.isBlank() -> ValidationResult.Invalid(ValidationMessages.PASSWORD_BLANK)

            else -> ValidationResult.Valid
        }
    }
}