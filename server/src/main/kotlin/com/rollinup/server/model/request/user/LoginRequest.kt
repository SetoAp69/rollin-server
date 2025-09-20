package com.rollinup.server.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName ("username")
    val username:String = "",
    @SerialName("password")
    val password:String = "",
//    @SerialName("jwt")
//    val jwt:String = "",
)