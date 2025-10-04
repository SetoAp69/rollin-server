package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.isEmail
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResetPasswordRequestRequest(
    @SerialName("email")
    val email: String = ""
) {
    private object ValidationMessages {
        const val EMAIL_INVALID = "Email format is invalid."
    }

    fun validation(): ValidationResult {
        return when {
            !email.isEmail() -> ValidationResult.Invalid(ValidationMessages.EMAIL_INVALID)
            else -> ValidationResult.Valid
        }
    }
}
