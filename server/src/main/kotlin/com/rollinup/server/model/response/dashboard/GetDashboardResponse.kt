package com.rollinup.server.model.response.dashboard

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetDashboardResponse(
    @SerialName("currentStatus")
    val currentStatus:String? = null,
    @SerialName("summary")
    val summary:Summary = Summary()
){
    @Serializable
    data class Summary(
        @SerialName("checkedIn")
        val attended:Int=0,
        @SerialName("late")
        val late:Int = 0,
        @SerialName("absent")
        val absent:Int=0,
        @SerialName("excused")
        val excused:Int = 0
    )
}