package com.rollinup.server.datasource.database.model.attendance

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ExportAttendanceDataEntity(
    @SerialName("fullName")
    val fullName: String = "",
    @SerialName("class")
    val classX: String = "",
    @SerialName("studentId")
    val studentId: String = "",
    @SerialName("data")
    val dataPerDate: List<AttendanceRecord> = emptyList(),
) {
    @Serializable
    data class AttendanceRecord(
        @SerialName("date")
        val sDate: String = "",
        @SerialName("status")
        val status: String = "",
    )
}