package com.rollinup.server.model.response.attendance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAttendanceByClassListResponse(
    @SerialName("record")
    val record: Int = 0,
    @SerialName("page")
    val page: Int = 0,
    @SerialName("summary")
    val summary: Summary = Summary(),
    @SerialName("data")
    val data: List<GetAttendanceByClassListDTO> = emptyList(),
) {
    @Serializable
    data class Summary(
        @SerialName("checkedIn")
        val checkedIn: Long = 0L,
        @SerialName("late")
        val late: Long = 0L,
        @SerialName("excused")
        val excused: Long = 0L,
        @SerialName("approvalPending")
        val approvalPending: Long = 0L,
        @SerialName("absent")
        val absent: Long = 0L,
        @SerialName("sick")
        val sick: Long = 0L,
        @SerialName("other")
        val other: Long = 0L,
    )

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
        val reason: String = "",
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
