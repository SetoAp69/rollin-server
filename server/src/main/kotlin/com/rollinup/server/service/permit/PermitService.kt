package com.rollinup.server.service.permit

import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.model.request.permit.PermitApprovalBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.permit.GetPermitByIdResponse
import com.rollinup.server.model.response.permit.GetPermitListByClassResponse
import com.rollinup.server.model.response.permit.GetPermitListByStudentResponse
import io.ktor.http.content.MultiPartData

interface PermitService {
    suspend fun getPermitByStudent(
        studentId: String,
        queryParams: GetPermitQueryParams,
    ): Response<GetPermitListByStudentResponse>

    suspend fun getPermitByClass(
        classKey: Int,
        queryParams: GetPermitQueryParams,
    ): Response<GetPermitListByClassResponse>

    suspend fun getPermitById(id: String): Response<GetPermitByIdResponse>

    suspend fun doApproval(id: String, body: PermitApprovalBody): Response<Unit>

    suspend fun createPermit(multiPart: MultiPartData): Response<Unit>

    suspend fun editPermit(multiPart: MultiPartData): Response<Unit>

}