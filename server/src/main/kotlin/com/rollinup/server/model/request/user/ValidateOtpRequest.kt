package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.isEmail
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateOtpRequest(
    @SerialName("email")
    val email: String = "",
    @SerialName("otp")
    val otp: String = ""
) {
    private object ValidationMessages {
        const val EMAIL_INVALID = "Email format is invalid."
        const val OTP_BLANK = "OTP cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            !email.isEmail() -> ValidationResult.Invalid(ValidationMessages.EMAIL_INVALID)
            otp.isBlank() -> ValidationResult.Invalid(ValidationMessages.OTP_BLANK)
            else -> ValidationResult.Valid
        }
    }

}
