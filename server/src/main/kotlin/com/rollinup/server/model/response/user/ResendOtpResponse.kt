package com.rollinup.server.model.response.user

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class ResendOtpResponse(
    @SerializedName("expiredAt")
    val expiredAt: String = "",
)