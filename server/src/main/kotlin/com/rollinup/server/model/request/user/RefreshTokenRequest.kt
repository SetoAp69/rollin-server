package com.rollinup.server.model.request.user

import io.ktor.server.plugins.requestvalidation.ValidationResult
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequest(
    @SerialName("refreshToken")
    val refreshToken: String = "",
) {
    private object ValidationMessages {
        const val EMPTY_REFRESH_TOKEN = "refresh token can't be empty."
    }

    fun validation(): ValidationResult {
        return when {
            refreshToken.isBlank() -> ValidationResult.Invalid(ValidationMessages.EMPTY_REFRESH_TOKEN)
            else -> ValidationResult.Valid
        }
    }
}
