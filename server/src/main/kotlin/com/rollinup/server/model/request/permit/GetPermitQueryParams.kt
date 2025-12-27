package com.rollinup.server.model.request.permit

import com.rollinup.server.util.Utils.toLocalDate
import java.time.LocalDate

data class GetPermitQueryParams(
    val limit: Int? = null,
    val page: Int? = null,
    val sortBy: String? = null,
    val order: String? = null,
    val search: String? = null,
    val listId:List<String>? = null,
    val isActive:Boolean = true,
    val type:List<String>? = null,
    val sDateRange:List<Long>? = null,
    val date:Long? = null,
    val status:List<String>? = null,
){
    val dateRange
        get() =  sDateRange?.map {
            it.toLocalDate()
        }
}
