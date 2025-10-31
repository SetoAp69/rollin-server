package com.rollinup.server.model.response.holiday

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetHolidayListResponse(
    @SerialName("record")
    val record:Int,
    @SerialName("data")
    val data:List<Holiday>
){
    @Serializable
    data class Holiday(
        @SerialName("id")
        val id:String = "",
        @SerialName("name")
        val name:String = "",
        @SerialName("date")
        val date:String = ""
    )
}
