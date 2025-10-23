package com.rollinup.server.datasource.database.repository.permit

import com.rollinup.server.datasource.database.model.permit.PermitByIdEntity
import com.rollinup.server.datasource.database.model.permit.PermitListEntity
import com.rollinup.server.model.request.permit.CreatePermitBody
import com.rollinup.server.model.request.permit.EditPermitBody
import com.rollinup.server.model.request.permit.GetPermitQueryParams

interface PermitRepository {
    fun getPermitList(
        queryParams: GetPermitQueryParams,
        studentId: String? = null,
        classKey: Int? = null,
    ): List<PermitListEntity>

    fun getPermitById(id: String): PermitByIdEntity?

    fun createPermit(body: CreatePermitBody): String

    fun editPermit(id: String, body: EditPermitBody)

    fun deletePermit(listId: List<String>)
}