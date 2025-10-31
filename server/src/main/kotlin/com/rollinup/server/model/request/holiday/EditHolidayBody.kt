package com.rollinup.server.model.request.holiday

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditHolidayBody(
    @SerialName("name")
    val name: String? = null,
    @SerialName("date")
    val date: Long? = null,
)
