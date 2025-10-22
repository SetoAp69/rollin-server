package com.rollinup.server.datasource.database.repository.attendance

import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.model.request.attendance.CreateAttendanceBody

interface AttendanceRepository {
    fun getAttendanceList(queryParams: AttendanceQueryParams):List<AttendanceEntity>

    fun getAttendanceById(id:String): AttendanceEntity?

    fun createAttendanceData(body: CreateAttendanceBody)

    fun getSummary(queryParams: AttendanceQueryParams): AttendanceSummaryEntity
}