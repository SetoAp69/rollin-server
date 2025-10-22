package com.rollinup.server.datasource.database.repository.permit

import com.rollinup.server.datasource.database.model.permit.PermitEntity
import com.rollinup.server.model.request.attendance.AttendanceQueryParams

interface PermitRepository {
    fun getPermitList(queryParams: AttendanceQueryParams):List<PermitEntity>
}