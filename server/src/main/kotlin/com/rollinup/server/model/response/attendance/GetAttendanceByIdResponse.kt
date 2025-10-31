package com.rollinup.server.model.response.attendance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAttendanceByIdResponse(
    @SerialName("id")
    val id: String = "",
    @SerialName("student")
    val student: User = User(),
    @SerialName("status")
    val status: String = "",
    @SerialName("checkedInAt")
    val checkedInAt:String? = null,
    @SerialName("updatedAt")
    val updatedAt: String = "",
    @SerialName("createdAt")
    val createdAt: String = "",
    @SerialName("permit")
    val permit: Permit? = null,

    ) {
    @Serializable
    data class User(
        @SerialName("id")
        val id: String = "",
        @SerialName("studentId")
        val studentId: String? = null,
        @SerialName("name")
        val name: String = "",
        @SerialName("class")
        val xClass: String? = null,
    )

    @Serializable
    data class Permit(
        @SerialName("id")
        val id: String = "",
        @SerialName("reason")
        val reason: String? = null,
        @SerialName("type")
        val type: String = "",
        @SerialName("start_time")
        val startTime: String = "",
        @SerialName("end_time")
        val endTime: String = "",
        @SerialName("note")
        val note: String? = null,
        @SerialName("attachment")
        val attachment: String = "",
        @SerialName("approval_note")
        val approvalNote: String? = null,
        @SerialName("approved_by")
        val approvedBy: User? = null,
        @SerialName("approved_at")
        val approvedAt: String? = null,
    )
}
