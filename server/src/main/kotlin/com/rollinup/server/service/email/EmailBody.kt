package com.rollinup.server.service.email


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EmailBody(
    @SerialName("sender")
    val sender: Sender = Sender(),
    @SerialName("to")
    val to: List<To> = emptyList(),
    @SerialName("subject")
    val subject: String = "",
    @SerialName("htmlContent")
    val htmlContent: String = ""
) {
    @Serializable
    data class Sender(
        @SerialName("name")
        val name: String = "",
        @SerialName("email")
        val email: String = ""
    )

    @Serializable
    data class To(
        @SerialName("email")
        val email: String = "",
        @SerialName("name")
        val name: String = "User"
    )
}