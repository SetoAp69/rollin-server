package com.rollinup.server.model.response.permit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPermitListResponse(
    @SerialName("status")
    val status:Int = 0,
    @SerialName("message")
    val message:String = "",
    @SerialName("data")
    val data :List<PermitListDTO> = emptyList()
) {
    @Serializable
    data class PermitListDTO(
        @SerialName("id")
        val id: String = "",
        @SerialName("date")
        val date:String = "",
        @SerialName("start_time")
        val startTime:String =""
    )
}
