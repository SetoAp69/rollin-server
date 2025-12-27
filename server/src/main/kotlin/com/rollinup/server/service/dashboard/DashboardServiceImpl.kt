package com.rollinup.server.service.dashboard

import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.mapper.DashboardMapper
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.dashboard.GetDashboardResponse
import com.rollinup.server.util.Utils
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.successGettingResponse
import java.time.LocalDate

class DashboardServiceImpl(
    val attendanceRepository: AttendanceRepository,
    val transactionManager: TransactionManager,
    val mapper: DashboardMapper,
) : DashboardService {
    override suspend fun getDashboardData(
        id: String,
        role: Role,
        date: LocalDate,
    ): Response<GetDashboardResponse> = transactionManager.suspendTransaction {
        val summary = attendanceRepository.getSummary(studentId = id)

        val dateMillis = date
            .atTime(0, 0, 0)
            .toInstant(Utils.getOffset())
            .toEpochMilli()

        val queryParams = GetAttendanceByStudentQueryParams(date=dateMillis)

        val currentAttendance = attendanceRepository
            .getAttendanceListByStudent(studentId = id, queryParams = queryParams)
            .firstOrNull()

        val currentStatus = currentAttendance?.status?.value

        val data = mapper.mapGetDashboardResponse(summary, currentStatus)

        return@suspendTransaction Response(
            status = 200,
            message = "Dashboard data".successGettingResponse(),
            data = data
        )
    }

}