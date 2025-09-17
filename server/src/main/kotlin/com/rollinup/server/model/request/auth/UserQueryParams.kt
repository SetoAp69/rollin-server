package com.rollinup.server.model.auth

data class UserQueryParams(
    val search:String? = null,
    val page:Int? = null,
    val limit:Int? = null,
    val sortBy:String? = null,
    val sortOrder:Int? = null,
    val gender:List<String>? = null,
    val role:List<String>? = null,
)
