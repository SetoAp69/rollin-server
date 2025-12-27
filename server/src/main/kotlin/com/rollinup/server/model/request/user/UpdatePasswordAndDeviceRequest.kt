package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.validatePassword
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePasswordAndDeviceRequest(
    @SerialName("password")
    val password: String = "",
    @SerialName("deviceId")
    val deviceId: String? = null,
    @SerialName("token")
    val token: String = "",
) {
    private object ValidationMessages {
        // Updated to be more granular based on the user's new checks
        const val PASSWORD_INVALID = "Password format is invalid."
        const val TOKEN_BLANK = "Token cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            !password.validatePassword() -> ValidationResult.Invalid(ValidationMessages.PASSWORD_INVALID)

            token.isBlank() -> ValidationResult.Invalid(ValidationMessages.TOKEN_BLANK)

            else -> {
                ValidationResult.Valid
            }
        }
    }
}
