package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceListResponse

class AttendanceMapper {
    fun mapAttendanceList(
        query: AttendanceQueryParams,
        data: List<AttendanceEntity>,
        summary: AttendanceSummaryEntity,
    ) = GetAttendanceListResponse(
        record = data.size,
        page = query.page ?: 1,
        data = data.map { attendance ->
            GetAttendanceListResponse.GetAttendanceListDTO(
                id = attendance.id,
                student = attendance.student.let {
                    GetAttendanceListResponse.User(
                        id = it.id,
                        name = it.name,
                        classX = it.classX ?: "",
                    )
                },
                status = attendance.status,
                checkedInAt = attendance.checkedInAt,
                createdAt = attendance.createdAt,
                updatedAt = attendance.updatedAt,
                permit = attendance.permit?.let {
                    GetAttendanceListResponse.Permit(
                        id = it.id,
                        reason = it.reason,
                        type = it.type.value,
                        startTime = it.startTime,
                        endTime = it.endTime
                    )
                }
            )
        },
        summary = GetAttendanceListResponse.Summary(
            checkedIn = summary.checkedIn,
            late = summary.late,
            excused = summary.excused,
            approvalPending = summary.approvalPending,
            absent = summary.absent,
            sick = summary.sick,
            other = summary.other
        )
    )

    fun mapAttendanceById(
        data: AttendanceEntity,
    ) = GetAttendanceByIdResponse(
        id = data.id,
        student = data.student.let { student ->
            GetAttendanceByIdResponse.User(
                id = student.id,
                studentId = student.studentId ?: "",
                name = student.name,
                xClass = student.classX
            )
        },
        status = data.status.value,
        updatedAt = data.updatedAt,
        createdAt = data.createdAt,
        permit = data.permit?.let { permit ->
            GetAttendanceByIdResponse.Permit(
                id = permit.id,
                reason = permit.reason,
                type = permit.type.value,
                startTime = permit.startTime,
                endTime = permit.endTime,
                note = permit.note,
                attachment = permit.attachment,
                approvalNote = permit.approvalNote,
                approvedBy = permit.approvedBy?.let { approver ->
                    GetAttendanceByIdResponse.User(
                        id = approver.id,
                        name = approver.name,
                    )
                },
                approvedAt = permit.approvedAt
            )
        }
    )


}