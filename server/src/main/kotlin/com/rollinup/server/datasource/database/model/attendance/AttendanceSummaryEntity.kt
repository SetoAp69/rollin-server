package com.rollinup.server.datasource.database.model.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus

data class AttendanceSummaryEntity(
    val checkedIn: Long = 0L,
    val sick: Long = 0L,
    val other: Long = 0L,
    val late: Long = 0L,
    val absent: Long = 0L,
    val excused: Long = 0L,
    val approvalPending: Long = 0L,
) {
    companion object {
        fun fromResultRow(
            statusCount: Map<AttendanceStatus, Long>,
            sickCount: Long,
            otherCount: Long,
        ) = AttendanceSummaryEntity(
            checkedIn = statusCount[AttendanceStatus.CHECKED_IN] ?: 0L,
            sick = sickCount,
            other = otherCount,
            late = statusCount[AttendanceStatus.LATE] ?: 0L,
            absent = statusCount[AttendanceStatus.ABSENT] ?: 0L,
            excused = statusCount[AttendanceStatus.EXCUSED] ?: 0L,
            approvalPending = statusCount[AttendanceStatus.APPROVAL_PENDING] ?: 0L,
        )


    }
}
