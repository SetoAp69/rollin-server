package com.rollinup.server.service.attendance

import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse
import io.ktor.http.content.MultiPartData

interface AttendanceService {
//    suspend fun getAttendance(queryParams: AttendanceQueryParams): Response<GetAttendanceListResponse>

    suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse>

    suspend fun createAttendanceData(multiPartData: MultiPartData, studentUserId: String,role: Role): Response<Unit>

    suspend fun getAttendanceListByStudent(queryParams: GetAttendanceByStudentQueryParams, studentId:String):Response<GetAttendanceByStudentListResponse>

    suspend fun getAttendanceListByClass(queryParams: GetAttendanceByClassQueryParams, classKey:Int): Response<GetAttendanceByClassListResponse>

    suspend fun updateAttendance(id:String, editBy:String, multiPartData: MultiPartData):Response<Unit>
}