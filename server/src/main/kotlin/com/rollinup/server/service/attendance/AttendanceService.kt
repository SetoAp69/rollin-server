package com.rollinup.server.service.attendance

import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse
import java.io.File

interface AttendanceService {
//    suspend fun getAttendance(queryParams: AttendanceQueryParams): Response<GetAttendanceListResponse>

    suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse>

    suspend fun createAttendanceData(
        userId: String,
        role: Role,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit>

    suspend fun getAttendanceListByStudent(
        queryParams: GetAttendanceByStudentQueryParams,
        studentId: String,
    ): Response<GetAttendanceByStudentListResponse>

    suspend fun getAttendanceListByClass(
        queryParams: GetAttendanceByClassQueryParams,
        classKey: Int,
    ): Response<GetAttendanceByClassListResponse>

    suspend fun updateAttendance(
        id: String,
        editBy: String,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit>
}