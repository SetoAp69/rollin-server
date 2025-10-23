package com.rollinup.server.model.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeleteByIdBody(
    @SerialName("listId")
    val listId:List<String> = emptyList()
)
