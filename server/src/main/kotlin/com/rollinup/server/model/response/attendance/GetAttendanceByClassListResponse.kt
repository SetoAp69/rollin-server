package com.rollinup.server.model.response.attendance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAttendanceByClassListResponse(
    @SerialName("record")
    val record: Int = 0,
    @SerialName("page")
    val page: Int = 0,
    @SerialName("data")
    val data: List<GetAttendanceByClassListDTO> = emptyList(),
) {

    @Serializable
    data class GetAttendanceByClassListDTO(
        @SerialName("student")
        val student: User = User(),
        @SerialName("Attendance")
        val attendance: Attendance? = null,
        @SerialName("permit")
        val permit: Permit? = null,
    )

    @Serializable
    data class Attendance(
        @SerialName("id")
        val id: String = "",
        @SerialName("checkedInAt")
        val checkedInAt: String? = null,
        @SerialName("status")
        val status: String = "",
        @SerialName("date")
        val date: String = "",
    )

    @Serializable
    data class Permit(
        @SerialName("id")
        val id: String = "",
        @SerialName("reason")
        val reason: String? = null,
        @SerialName("type")
        val type: String = "",
        @SerialName("startTime")
        val startTime: String = "",
        @SerialName("endTime")
        val endTime: String = "",
    )

    @Serializable
    data class User(
        @SerialName("id")
        val id: String = "",
        @SerialName("name")
        val name: String = "",
        @SerialName("studentId")
        val studentId: String? = null,
//        val classX: String = "",
    )
}
