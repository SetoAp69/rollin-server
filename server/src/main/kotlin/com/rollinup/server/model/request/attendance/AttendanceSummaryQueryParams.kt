package com.rollinup.server.model.request.attendance

data class AttendanceSummaryQueryParams(
    val classX: Int? = null,
    val studentUserId: String? = null,
    val dateRange:List<Long>? = null
)
