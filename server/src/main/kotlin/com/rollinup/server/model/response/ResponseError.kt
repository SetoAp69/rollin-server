package com.rollinup.server.model.response

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResponseError(
    @SerialName("status")
    val status: Int = 0,
    @SerialName("message")
    val message: String = ""
) {
    val statusCode
        get() = HttpStatusCode.fromValue(status)
}
