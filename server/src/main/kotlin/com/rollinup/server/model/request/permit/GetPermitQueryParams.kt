package com.rollinup.server.model.request.permit

data class GetPermitQueryParams(
    val limit: Int? = null,
    val page: Int? = null,
    val sortBy: String? = null,
    val order: String? = null,
    val search: String? = null,
    val isActive:Boolean = true,
    val type:List<String>? = null,
    val dateRange:List<Long>? = null,
    val date:Long? = null,
    val status:List<String>? = null,
)
