package com.rollinup.server.datasource.apiservice.model.request.registration

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginBody(
    @SerialName("username")
    val username:String = "",
    @SerialName("password")
    val password:String = "",
    @SerialName("grant_type")
    val grantType:String = "",
    @SerialName("client_id")
    val clientId:String = "",
    @SerialName("client_secret")
    val clientSecret:String = "",
    @SerialName("scope")
    val scope:String = ""
)
