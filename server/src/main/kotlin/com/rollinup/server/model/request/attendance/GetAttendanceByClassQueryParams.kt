package com.rollinup.server.model.request.attendance

data class GetAttendanceByClassQueryParams(
    val limit: Int? = null,
    val page: Int? = null,
    val sortBy: String? = null,
    val order: String? = null,
    val search: String? = null,
    val status: List<String>? = null,
    val date: Long? = null,
)
