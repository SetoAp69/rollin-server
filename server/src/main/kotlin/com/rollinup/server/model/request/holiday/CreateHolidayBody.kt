package com.rollinup.server.model.request.holiday

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateHolidayBody(
    @SerialName("name")
    val name: String = "",
    @SerialName("date")
    val date: Long = 0L,
)
