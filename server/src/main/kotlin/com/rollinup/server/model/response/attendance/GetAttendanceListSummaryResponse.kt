package com.rollinup.server.model.response.attendance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetAttendanceListSummaryResponse(
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