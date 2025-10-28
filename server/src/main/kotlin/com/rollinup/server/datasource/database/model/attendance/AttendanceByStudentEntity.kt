package com.rollinup.server.datasource.database.model.attendance


import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.table.AttendanceTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.model.PermitType
import org.jetbrains.exposed.v1.core.ResultRow

data class AttendanceByStudentEntity(
    val id: String = "",
    val status: AttendanceStatus = AttendanceStatus.ALPHA,
    val checkedInAt: String? = null,
    val date: String = "",
    val permit: Permit? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
) {

    data class Permit(
        val id: String = "",
        val reason: String? = null,
        val type: PermitType = PermitType.DISPENSATION,
        val startTime: String = "",
        val endTime: String = "",
    )

    companion object {
        fun fromResultRow(row: ResultRow) = AttendanceByStudentEntity(
            id = row[AttendanceTable._id].toString(),
            status = row[AttendanceTable.status],
            checkedInAt = row.getOrNull(AttendanceTable.checkedInAt).toString(),
            date = row[AttendanceTable.date].toString(),
            permit = row.getOrNull(AttendanceTable.permit)?.let {
                Permit(
                    id = row[PermitTable._id].toString(),
                    reason = row[PermitTable.reason],
                    type = row[PermitTable.type],
                    startTime = row[PermitTable.startTime].toString(),
                    endTime = row[PermitTable.endTime].toString()
                )
            },
            createdAt = row[AttendanceTable.createdAt].toString(),
            updatedAt = row[AttendanceTable.updatedAt].toString(),
        )
    }
}

