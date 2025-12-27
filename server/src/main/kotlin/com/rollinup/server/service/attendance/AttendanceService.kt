package com.rollinup.server.service.attendance

import com.rollinup.server.model.request.attendance.CreateAttendanceRequest
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.request.attendance.GetExportAttendanceQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceListSummaryResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse
import com.rollinup.server.model.response.attendance.GetExportAttendanceResponse
import java.io.File

interface AttendanceService {
//    suspend fun getAttendance(queryParams: AttendanceQueryParams): Response<GetAttendanceListResponse>

    suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse>

    suspend fun checkIn(
        id: String,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit>

    suspend fun createAttendanceData(
        requestBody: CreateAttendanceRequest,
    ): Response<Unit>

    suspend fun getAttendanceListByStudent(
        queryParams: GetAttendanceByStudentQueryParams,
        studentId: String,
    ): Response<GetAttendanceByStudentListResponse>

    suspend fun getAttendanceListByStudentSummary(
        dateRange:List<Long>?,
        studentId:String,
    ): Response<GetAttendanceListSummaryResponse>

    suspend fun getAttendanceListByClass(
        queryParams: GetAttendanceByClassQueryParams,
        classKey: Int,
    ): Response<GetAttendanceByClassListResponse>

    suspend fun getAttendanceListByClassSummary(
        classKey: Int,
        date: Long,
    ): Response<GetAttendanceListSummaryResponse>

    suspend fun updateAttendance(
        id: String,
        editBy: String,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit>

    suspend fun getExportAttendanceData(
        queryParams: GetExportAttendanceQueryParams,
    ):Response<GetExportAttendanceResponse>
}