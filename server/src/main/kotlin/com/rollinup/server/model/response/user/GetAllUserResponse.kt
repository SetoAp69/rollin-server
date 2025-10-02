package com.rollinup.server.model.response.user

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAllUserResponse(
    @SerialName("status")
    val status: Int = 0,
    @SerialName("message")
    val message: String,
    @SerialName("record")
    val record: Int,
    @SerialName("page")
    val page: Int,
    @SerialName("data")
    val data: List<UserDTO>
) {
    val statusCode
        get() = HttpStatusCode.fromValue(status)
}
