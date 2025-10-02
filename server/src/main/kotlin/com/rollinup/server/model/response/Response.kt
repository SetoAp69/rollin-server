package com.rollinup.server.model.response

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Response<T>(
    @SerialName("status")
    val status: Int = 0,
    @SerialName("message")
    val message: String = "",
    @SerialName("data")
    val data: T? = null
) {
    val statusCode
        get() = HttpStatusCode.fromValue(status)
}
