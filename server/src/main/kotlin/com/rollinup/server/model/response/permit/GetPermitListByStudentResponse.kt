package com.rollinup.server.model.response.permit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetPermitListByStudentResponse(
    @SerialName("record")
    val record: Int = 0,
    @SerialName("page")
    val page: Int = 1,
    @SerialName("data")
    val data: List<PermitListDTO> = emptyList(),
) {
    @Serializable
    data class PermitListDTO(
        @SerialName("id")
        val id: String = "",
        @SerialName("student")
        val studentId:String = "",
        @SerialName("name")
        val name:String = "",
        @SerialName("date")
        val date: String = "",
        @SerialName("startTime")
        val startTime: String = "",
        @SerialName("reason")
        val reason: String? = null,
        @SerialName("approvalStatus")
        val approvalStatus: String = "",
        @SerialName("type")
        val type: String = "",
        @SerialName("endTime")
        val endTime: String = "",
        @SerialName("createdAt")
        val createdAt:String = ""
    )

}