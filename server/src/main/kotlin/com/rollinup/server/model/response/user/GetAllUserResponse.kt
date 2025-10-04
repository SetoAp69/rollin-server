package com.rollinup.server.model.response.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAllUserResponse(
    @SerialName("record")
    val record: Int,
    @SerialName("page")
    val page: Int,
    @SerialName("data")
    val data: List<UserDTO>
)
