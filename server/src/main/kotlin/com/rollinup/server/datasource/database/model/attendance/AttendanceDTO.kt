package com.rollinup.server.datasource.database.model.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.model.PermitType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AttendanceDTO(
    @SerialName("id")
    val id:String = "",
    @SerialName("user")
    val user:User = User(),
    @SerialName("status")
    val status: AttendanceStatus = AttendanceStatus.ALPHA,
    @SerialName("checkIn")
    val checkedInAt:String? = null,
//    @SerialName("permitStart")
//    val permitStart:String? = null,
//    @SerialName("permitEnd")
//    val permitEnd:String? = null,
    @SerialName("duration")
    val duration:String = "",
    @SerialName("createdAt")
    val createdAt:String = "",
    @SerialName("updatedAt")
    val updatedAt:String = "",
    @SerialName("permit")
    val permit: PermitType? = null,
    @SerialName("approvedBy")
    val approvedBy:User? = null,
    @SerialName("approvalNote")
    val approvalNote:String? = null,
    @SerialName("approvedAt")
    val approvedAt:String? = null,
    @SerialName("date")
    val date:String? = null
){
    @Serializable
    data class User(
        @SerialName("id")
        val id:String = "",
        @SerialName("name")
        val name:String = "",
        @SerialName("username")
        val userName:String = "",
        @SerialName("class")
        val classX :String = ""
    )
}
