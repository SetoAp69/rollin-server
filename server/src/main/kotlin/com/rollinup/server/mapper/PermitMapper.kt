package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.permit.PermitByIdEntity
import com.rollinup.server.datasource.database.model.permit.PermitListEntity
import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.model.response.permit.GetPermitByIdResponse
import com.rollinup.server.model.response.permit.GetPermitListByClassResponse
import com.rollinup.server.model.response.permit.GetPermitListByStudentResponse

class PermitMapper {
    fun mapPermitByStudent(
        data: List<PermitListEntity>,
        queryParams: GetPermitQueryParams,
    ) = GetPermitListByStudentResponse(
        data = data.map { permit ->
            GetPermitListByStudentResponse.PermitListDTO(
                id = permit.id,
                studentId = permit.student.id,
                name = permit.name,
                date = permit.date,
                startTime = permit.permitStart,
                reason = permit.reason,
                approvalStatus = permit.approvalStatus.value,
                type = permit.type.value,
                endTime = permit.permitEnd,
                createdAt = permit.createdAt
            )
        },
        record = data.size,
        page = queryParams.page ?: 1,
    )

    fun mapPermitByClass(
        data: List<PermitListEntity>,
        queryParams: GetPermitQueryParams,
    ) = GetPermitListByClassResponse(
        record = data.size,
        page = queryParams.page ?: 1,
        data = data.map { permit ->
            GetPermitListByClassResponse.PermitListDTO(
                id = permit.id,
                name = permit.name,
                date = permit.date,
                startTime = permit.permitStart,
                reason = permit.reason,
                approvalStatus = permit.approvalStatus.value,
                type = permit.type.value,
                endTime = permit.permitEnd,
                student = permit.student.let { student ->
                    GetPermitListByClassResponse.User(
                        id = student.id,
                        name = student.name,
                        xClass = student.classX
                    )
                },
                createdAt = permit.createdAt
            )
        }
    )

    fun mapPermitById(
        data: PermitByIdEntity,
    ) = GetPermitByIdResponse(
        id = data.id,
        date = data.date,
        name = data.name,
        student = data.student.let { student ->
            GetPermitByIdResponse.User(
                id = student.id,
                name = student.name,
                username = student.username,
                studentId = student.studentId,
                xClass = student.classX
            )
        },
        startTime = data.permitStart,
        endTime = data.permitEnd,
        attachment = data.attachment,
        note = data.note,
        reason = data.reason,
        createdAt = data.createdAt,
        updatedAt = data.updatedAt,
        approvalStatus = data.approvalStatus.value,
        approvalNote = data.approvalNote,
        approvedBy = data.approvedBy?.let { approver ->
            GetPermitByIdResponse.User(
                id = approver.id,
                name = approver.name,
                username = approver.username,
            )
        },
        approvedAt = data.approvedAt
    )
}