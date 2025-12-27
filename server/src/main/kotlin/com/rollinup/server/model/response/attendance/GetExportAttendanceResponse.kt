package com.rollinup.server.model.response.attendance

import com.rollinup.server.datasource.database.model.attendance.ExportAttendanceDataEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetExportAttendanceResponse(
    @SerialName("dateRange")
    val dateRange: List<String> = emptyList(),
    @SerialName("data")
    val data: List<ExportAttendanceDataEntity> = emptyList(),
)
