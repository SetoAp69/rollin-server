package com.rollinup.server.model.request.attendance

data class AttendanceQueryParams(
    val limit: Int? = null,
    val page: Int? = null,
    val sortBy: String? = null,
    val order: String? = null,
    val search: String? = null,
    val status: List<String>? = null,
    val xClass: List<Int>? = null,
    val dateRange: List<Long>? = null,
    val studentId:String? = null
){
    val searchPattern
        get() = "%$search%"
}
