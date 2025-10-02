package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.validatePassword
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequest(
    @SerialName("token")
    val token: String = "",
    @SerialName("newPassword")
    val newPassword: String = ""
){
    private object ValidationMessages {
        const val TOKEN_BLANK = "Token cannot be empty."
        const val PASSWORD_INVALID= "Password format is invalid."
    }

    fun validation(): ValidationResult{
        return when{
            token.isBlank() -> ValidationResult.Invalid(ValidationMessages.TOKEN_BLANK)
            newPassword.validatePassword() -> ValidationResult.Invalid(ValidationMessages.PASSWORD_INVALID)
            else -> ValidationResult.Valid
        }
    }
}
