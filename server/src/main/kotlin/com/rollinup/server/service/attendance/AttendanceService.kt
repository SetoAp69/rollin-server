package com.rollinup.server.service.attendance

import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceListResponse
import io.ktor.http.content.MultiPartData

interface AttendanceService {
    suspend fun getAttendance(queryParams: AttendanceQueryParams): Response<GetAttendanceListResponse>

    suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse>

    suspend fun createAttendanceData(multiPartData: MultiPartData, studentUserId: String): Response<Unit>

}