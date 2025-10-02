package com.rollinup.server.model.request.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserQueryParams(
    @SerialName("search")
    val search: String? = null,
    @SerialName("page")
    val page: Int? = null,
    @SerialName("limit")
    val limit: Int? = null,
    @SerialName("sortBy")
    val sortBy: String? = null,
    @SerialName("sortOrder")
    val sortOrder: String? = null,
    @SerialName("gender")
    val gender: List<String>? = null,
    @SerialName("role")
    val role: List<String>? = null,
)
