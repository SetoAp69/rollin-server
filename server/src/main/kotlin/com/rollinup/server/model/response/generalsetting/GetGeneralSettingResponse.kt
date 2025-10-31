package com.rollinup.server.model.response.generalsetting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetGeneralSettingResponse(
    @SerialName("semesterStart")
    val semesterStart: String = "",
    @SerialName("semesterEnd")
    val semesterEnd: String = "",
    @SerialName("updatedAt")
    val updatedAt: String = "",
    @SerialName("schoolPeriodStart")
    val schoolPeriodStart: String = "",
    @SerialName("schoolPeriodEnd")
    val schoolPeriodEnd: String = "",
    @SerialName("checkInPeriodStart")
    val checkInPeriodStart: String = "",
    @SerialName("checkInPeriodEnd")
    val checkInPeriodEnd: String = "",
    @SerialName("latitude")
    val latitude: Double = 0.0,
    @SerialName("longitude")
    val longitude: Double = 0.0,
    @SerialName("radius")
    val radius: Double = 0.0,
    @SerialName("modifiedBY")
    val modifiedBy: ModifiedBy = ModifiedBy(),
) {
    @Serializable
    data class ModifiedBy(
        @SerialName("id")
        val id: String = "",
        @SerialName("name")
        val name: String = "",
    )
}
