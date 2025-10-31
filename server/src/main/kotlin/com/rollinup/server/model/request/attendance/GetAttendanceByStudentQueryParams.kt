package com.rollinup.server.model.request.attendance

data class GetAttendanceByStudentQueryParams(
    val search:String? = null,
    val limit:Int? = null,
    val page:Int? = null,
    val dateRange:List<Long>? = null
)
