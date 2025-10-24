package com.rollinup.server.datasource.database.model.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.table.AttendanceTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.PermitType
import org.jetbrains.exposed.v1.core.ResultRow

data class AttendanceByClassEntity(
    val user: User = User(),
    val attendance: Attendance? = null,
    val permit: Permit? = null,
) {
    data class User(
        val id: String = "",
        val name: String = "",
        val studentId: String? = null,
        val username: String = "",
    )

    data class Attendance(
        val id: String = "",
        val status: AttendanceStatus = AttendanceStatus.ALPHA,
        val checkedInAt: String? = null,
        val date: String = "",
    )

    data class Permit(
        val id: String = "",
        val reason: String? = null,
        val type: PermitType = PermitType.DISPENSATION,
        val startTime: String = "",
        val endTime: String = "",
    )

    companion object {
        fun fromResultRow(row: ResultRow) = AttendanceByClassEntity(
            user = User(
                id = row[UserTable.user_id].toString(),
                studentId = row[UserTable.studentId],
                username = row[UserTable.username],
                name = row[UserTable.firstName] + " " + row[UserTable.lastName]
            ),
            attendance = row.getOrNull(AttendanceTable._id)?.let {
                Attendance(
                    id = row[AttendanceTable._id].toString(),
                    status = row[AttendanceTable.status],
                    checkedInAt = row.getOrNull(AttendanceTable.checkedInAt).toString(),
                    date = row[AttendanceTable.date].toString(),
                )
            },
            permit = row.getOrNull(PermitTable._id)?.let {
                Permit(
                    id = row[PermitTable._id].toString(),
                    reason = row[PermitTable.reason],
                    type = row[PermitTable.type],
                    startTime = row[PermitTable.startTime].toString(),
                    endTime = row[PermitTable.endTime].toString()
                )
            }
        )
    }
}
