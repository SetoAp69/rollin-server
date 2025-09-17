package com.rollinup.server.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    @SerialName ("username")
    val username:String = "",
    @SerialName("password")
    val password:String = "",
    @SerialName("jwt")
    val jwt:String = "",
    ////Could be removed
    @SerialName("grant_type")
    val grantType:String = "",
    @SerialName("client_id")
    val clientId:String = "",
    @SerialName("client_secret")
    val clientSecret:String = "",
    @SerialName("scope")
    val scope:String = "",
)