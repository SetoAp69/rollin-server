package com.rollinup.server.model.response.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ValidateResetOtpResponse(
    @SerialName("reset_token")
    val resetToken: String = ""
)