package com.rollinup.server.datasource.apiservice.model.request.registration

import com.google.gson.annotations.SerializedName

data class GetAdminAccessTokenBody(
    @SerializedName("client_id")
    val clientId: String = "",
    @SerializedName("username")
    val userName: String = "",
    @SerializedName("password")
    val password: String = "",
    @SerializedName("grant_type")
    val grantType: String = ""
)
