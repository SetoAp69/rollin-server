package com.rollinup.server.model.response.permit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPermitByIdResponse(
    @SerialName("id")
    val id: String = "",
    @SerialName("date")
    val date: String = "",
    @SerialName("name")
    val name: String = "",
    @SerialName("student")
    val student: User = User(),
    @SerialName("startTime")
    val startTime: String = "",
    @SerialName("endTime")
    val endTime: String = "",
    @SerialName("note")
    val note: String? = null,
    @SerialName("reason")
    val reason: String? = null,
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("updatedAt")
    val updatedAt: String = "",
    @SerialName("approvalStatus")
    val approvalStatus: String = "",
    @SerialName("approvalNote")
    val approvalNote: String? = null,
    @SerialName("approvedBy")
    val approvedBy: User? = null,
    @SerialName("approvedAt")
    val approvedAt: String? = null,
) {
    @Serializable
    data class User(
        @SerialName("id")
        val id: String = "",
        @SerialName("name")
        val name: String = "",
        @SerialName("username")
        val username: String = "",
        @SerialName("studentId")
        val studentId: String? = null,
        @SerialName("class")
        val xClass: String? = null,
    )
}
