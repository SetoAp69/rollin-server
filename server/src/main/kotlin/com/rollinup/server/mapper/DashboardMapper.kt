package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.model.response.dashboard.GetDashboardResponse

class DashboardMapper {
    fun mapGetDashboardResponse(
        summary: AttendanceSummaryEntity,
        currentStatus:String?
    ) = GetDashboardResponse(
        currentStatus = currentStatus,
        summary = GetDashboardResponse.Summary(
            attended = summary.checkedIn.toInt(),
            late = summary.late.toInt(),
            absent = summary.absent.toInt(),
            excused = summary.excused.toInt()
        )
    )
}