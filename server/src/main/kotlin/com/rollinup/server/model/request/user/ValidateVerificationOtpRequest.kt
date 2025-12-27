package com.rollinup.server.model.request.user

import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateVerificationOtpRequest(
    @SerialName("otp")
    val otp: String = ""
) {
    private object ValidationMessages {
        const val OTP_BLANK = "OTP cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            otp.isBlank() -> ValidationResult.Invalid(ValidationMessages.OTP_BLANK)
            else -> ValidationResult.Valid
        }
    }

}
