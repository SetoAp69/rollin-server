package com.rollinup.server.datasource.database.repository.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.attendance.AttendanceByClassEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceByStudentEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.model.request.attendance.AttendanceSummaryQueryParams
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import java.time.LocalDate

interface AttendanceRepository {

    fun getAttendanceById(id: String): AttendanceEntity?

    fun createAttendanceData(body: CreateAttendanceBody): String

    fun getSummary(queryParams: AttendanceSummaryQueryParams): AttendanceSummaryEntity

    fun isCheckedIn(userId: String): Boolean

    fun getAttendanceListByClass(
        queryParams: GetAttendanceByClassQueryParams,
        classKey: Int,
    ): List<AttendanceByClassEntity>

    fun getAttendanceListByStudent(
        queryParams: GetAttendanceByStudentQueryParams,
        studentId: String,
    ): List<AttendanceByStudentEntity>

    fun updateAttendanceData(listId: List<String>, body: EditAttendanceBody)

    fun deleteAttendanceData(listId: List<String>)

    fun updatePermit(id: String, permitId: String?)

    fun createAttendanceFromPermit(
        permitId: String,
        studentId: String,
        dates: List<LocalDate>,
        status: AttendanceStatus,
    )

    fun getAttendanceListByPermit(listId: List<String>): List<AttendanceEntity>
}