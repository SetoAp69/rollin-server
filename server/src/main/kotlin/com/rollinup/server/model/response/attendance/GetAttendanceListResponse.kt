package com.rollinup.server.model.response.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAttendanceListResponse(
    @SerialName("record")
    val record: Int = 0,
    @SerialName("page")
    val page: Int = 0,
    @SerialName("summary")
    val summary: Summary = Summary(),
    @SerialName("data")
    val data: List<GetAttendanceListDTO> = emptyList(),
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
    data class GetAttendanceListDTO(
        @SerialName("id")
        val id: String = "",
        @SerialName("student")
        val student: User = User(),
        @SerialName("status")
        val status: AttendanceStatus = AttendanceStatus.ALPHA,
        @SerialName("checkIn")
        val checkedInAt: String? = null,
        @SerialName("permit")
        val permit: Permit? = null,
        @SerialName("createdAt")
        val createdAt: String = "",
        @SerialName("updatedAt")
        val updatedAt: String = "",
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
        @SerialName("class")
        val classX: String = "",
    )
}
