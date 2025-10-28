package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.attendance.AttendanceByClassEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceByStudentEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse

class AttendanceMapper {

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

    fun mapAttendanceListByClass(
        data: List<AttendanceByClassEntity>,
        summary: AttendanceSummaryEntity,
        queryParams: GetAttendanceByClassQueryParams,
    ) =
        GetAttendanceByClassListResponse(
            record = data.size,
            page = queryParams.page ?: 1,
            summary = GetAttendanceByClassListResponse.Summary(
                checkedIn = summary.checkedIn,
                late = summary.late,
                excused = summary.excused,
                approvalPending = summary.approvalPending,
                absent = summary.absent,
                sick = summary.sick,
                other = summary.other
            ),
            data = data.map {
                GetAttendanceByClassListResponse.GetAttendanceByClassListDTO(
                    student = GetAttendanceByClassListResponse.User(
                        id = it.user.id,
                        name = it.user.name,
                        studentId = it.user.studentId
                    ),
                    attendance = it.attendance?.let { att ->
                        GetAttendanceByClassListResponse.Attendance(
                            id = att.id,
                            checkedInAt = att.checkedInAt,
                            status = att.status.value,
                            date = att.date
                        )
                    },
                    permit = it.permit?.let { permit ->
                        GetAttendanceByClassListResponse.Permit(
                            id = permit.id,
                            reason = permit.reason,
                            type = permit.type.value,
                            startTime = permit.startTime,
                            endTime = permit.endTime
                        )
                    }
                )
            }
        )

    fun mapAttendanceListByStudent(
        data: List<AttendanceByStudentEntity>,
        summary: AttendanceSummaryEntity,
        queryParams: GetAttendanceByStudentQueryParams,
    ) =
        GetAttendanceByStudentListResponse(
            record = data.size,
            page = queryParams.page ?: 1,
            summary = GetAttendanceByStudentListResponse.Summary(
                checkedIn = summary.checkedIn,
                late = summary.late,
                excused = summary.excused,
                approvalPending = summary.approvalPending,
                absent = summary.absent,
                sick = summary.sick,
                other = summary.other
            ),
            data = data.map { att ->
                GetAttendanceByStudentListResponse.GetAttendanceByStudentListDTO(
                    id = att.id,
                    status = att.status,
                    checkedInAt = att.checkedInAt,
                    permit = att.permit?.let { permit ->
                        GetAttendanceByStudentListResponse.Permit(
                            id = permit.id,
                            reason = permit.reason,
                            type = permit.type.value,
                            startTime = permit.startTime,
                            endTime = permit.endTime
                        )
                    },
                    createdAt = att.createdAt,
                    updatedAt = att.updatedAt
                )
            }
        )


}