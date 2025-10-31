package com.rollinup.server.model.request.generalsetting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EditGeneralSettingBody(
    @SerialName("semesterStart")
    val semesterStart: Long? = null,
    @SerialName("semesterEnd")
    val semesterEnd: Long? = null,
    @SerialName("schoolPeriodStart")
    val schoolPeriodStart: Long? = null,
    @SerialName("schoolPeriodEnd")
    val schoolPeriodEnd: Long? = null,
    @SerialName("checkInPeriodStart")
    val checkInPeriodStart: Long? = null,
    @SerialName("checkInPeriodEnd")
    val checkInPeriodEnd: Long? = null,
    @SerialName("latitude")
    val latitude: Double? = null,
    @SerialName("longitude")
    val longitude: Double? = null,
    @SerialName("radius")
    val radius: Double? = null,
)
