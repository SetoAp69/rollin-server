package com.rollinup.server.model.response.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenResponse(
    @SerialName("accessToken")
    val accessToken: String = "",
)
