package com.rollinup.server.model.request.user

import com.rollinup.server.util.Utils.isEmail
import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegisterDeviceBody(
    @SerialName("deviceId")
    val deviceId: String = "",
) {
    private object ValidationMessages {
        // Updated to be more granular based on the user's new checks
        const val DEVICE_ID_BLANK = "Device Id cannot be empty."
    }

    fun validation(): ValidationResult {
        return when {
            deviceId.isBlank() -> {
                ValidationResult.Invalid(ValidationMessages.DEVICE_ID_BLANK)
            }

            else -> {
                ValidationResult.Valid
            }
        }
    }
}
