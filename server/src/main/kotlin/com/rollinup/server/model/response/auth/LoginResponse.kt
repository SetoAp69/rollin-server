package com.rollinup.server.model.response.auth

import com.rollinup.server.model.response.user.UserDTO
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    @SerialName("data")
    val data: UserDTO = UserDTO(),
    @SerialName("access_token")
    val accessToken: String = "",
    @SerialName("refresh_token")
    val refreshToken: String = ""
)
