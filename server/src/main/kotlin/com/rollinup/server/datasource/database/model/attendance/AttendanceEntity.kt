package com.rollinup.server.datasource.database.model.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.table.AttendanceTable
import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.datasource.database.model.PermitType
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow

data class AttendanceEntity(
    val id: String = "",
    val student: User = User(),
    val status: AttendanceStatus = AttendanceStatus.ALPHA,
    val checkedInAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val date: String = "",
    val permit: Permit? = null,
    val location: Location? = null,
    val attachment: String? = null,
) {
    data class User(
        val id: String = "",
        val studentId: String? = null,
        val username: String = "",
        val name: String = "",
        val classX: String? = null,
    )

    data class Permit(
        val id: String = "",
        val reason: String? = null,
        val type: PermitType = PermitType.DISPENSATION,
        val startTime: String = "",
        val endTime: String = "",
        val note: String? = null,
        val attachment: String = "",
        val approvalNote: String? = null,
        val approvedBy: User? = null,
        val approvedAt: String? = null,
    )

    data class Location(
        val latitude: Double = 0.0,
        val longitude: Double = 0.0,
    )

    companion object {
        fun fromResultRow(row: ResultRow): AttendanceEntity {
            return AttendanceEntity(
                id = row[AttendanceTable._id].toString(),
                status = row[AttendanceTable.status],
                checkedInAt = row.getOrNull(AttendanceTable.checkedInAt)?.toString(),
                createdAt = row[AttendanceTable.createdAt].toString(),
                updatedAt = row[AttendanceTable.updatedAt].toString(),
                date = row[AttendanceTable.date].toString(),
                permit = row.getOrNull(AttendanceTable.permit)?.let{
                    Permit(
                        id = row[PermitTable._id].toString(),
                        type = row[PermitTable.type]
                    )
                }

            )
        }

        fun fromResultRowById(
            row: ResultRow,
            student: Alias<UserTable>,
            approver: Alias<UserTable>,
        ): AttendanceEntity {
            return AttendanceEntity(
                id = row[AttendanceTable._id].toString(),
                student = User(
                    id = row[student[UserTable.user_id]].toString(),
                    username = row[student[UserTable.username]],
                    name = row[student[UserTable.firstName]] + " " + row[student[UserTable.lastName]],
                    classX = row[ClassTable.name],
                    studentId = row.getOrNull(UserTable.studentId)
                ),
                status = row[AttendanceTable.status],
                checkedInAt = row.getOrNull(AttendanceTable.checkedInAt).toString(),
                createdAt = row[AttendanceTable.createdAt].toString(),
                updatedAt = row[AttendanceTable.updatedAt].toString(),
                date = row[AttendanceTable.date].toString(),
                permit = row.getOrNull(AttendanceTable.permit)?.let {
                    Permit(
                        id = it.toString(),
                        reason = row[PermitTable.reason],
                        type = row[PermitTable.type],
                        startTime = row[PermitTable.startTime].toString(),
                        endTime = row[PermitTable.endTime].toString(),
                        note = row[PermitTable.note],
                        attachment = row[PermitTable.attachment],
                        approvalNote = row.getOrNull(PermitTable.approvalNote),
                        approvedBy = row.getOrNull(PermitTable.approvedBy)?.let {
                            User(
                                id = it.toString(),
                                username = row[approver[UserTable.username]],
                                name = row[approver[UserTable.firstName]] + " " + row[approver[UserTable.lastName]],
                            )
                        },
                        approvedAt = row.getOrNull(PermitTable.approvedAt)?.toString()
                    )
                },
                location = with(
                    Pair(
                        row.getOrNull(AttendanceTable.latitude),
                        row.getOrNull(AttendanceTable.longitude)
                    )
                ) {
                    if (first == null || second == null) null
                    else Location(
                        latitude = first!!,
                        longitude = second!!
                    )
                },
                attachment = row.getOrNull(AttendanceTable.attachment)
            )
        }
    }
}
