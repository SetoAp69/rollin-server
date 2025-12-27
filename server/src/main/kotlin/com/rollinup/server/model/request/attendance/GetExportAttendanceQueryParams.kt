package com.rollinup.server.model.request.attendance

import java.time.LocalDate

data class GetExportAttendanceQueryParams(
    val classKey: Int? = null,
    val dateRange:List<LocalDate>? = null
)
