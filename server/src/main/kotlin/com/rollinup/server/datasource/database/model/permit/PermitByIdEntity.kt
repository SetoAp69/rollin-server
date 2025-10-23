package com.rollinup.server.datasource.database.model.permit

import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.ApprovalStatus
import com.rollinup.server.model.PermitType
import org.jetbrains.exposed.v1.core.Alias
import org.jetbrains.exposed.v1.core.ResultRow

data class PermitByIdEntity(
    val id: String = "",
    val name: String = "",
    val user: User = User(),
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVAL_PENDING,
    val type: PermitType = PermitType.DISPENSATION,
    val reason: String? = null,
    val note: String? = null,
    val permitStart: String = "",
    val permitEnd: String = "",
    val approvedBy: User? = null,
    val approvalNote: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val attachment: String = "",
    val approvedAt: String? = null,
) {
    data class User(
        val id: String = "",
        val name: String = "",
        val username: String = "",
    )

    companion object {
        fun fromResultRow(
            row: ResultRow,
            student: Alias<UserTable>,
            approver: Alias<UserTable>,
        ): PermitByIdEntity {
            return PermitByIdEntity(
                id = row[PermitTable._id].toString(),
                name = row[PermitTable.name],
                user = User(
                    id = row[student[UserTable.user_id]].toString(),
                    name = row[student[UserTable.firstName]] + " " + row[student[UserTable.lastName]],
                    username = row[student[UserTable.username]]
                ),
                approvalStatus = row[PermitTable.approvalStatus],
                type = row[PermitTable.type],
                reason = row[PermitTable.reason],
                note = row[PermitTable.note],
                permitStart = row[PermitTable.startTime].toString(),
                permitEnd = row[PermitTable.endTime].toString(),
                approvedBy = row.getOrNull(PermitTable.approvedBy).let {
                    if (it == null) null
                    else User(
                        id = it.toString(),
                        name = row[approver[UserTable.firstName]] + " " + row[approver[UserTable.lastName]],
                        username = row[approver[UserTable.username]]
                    )
                },
                approvalNote = row.getOrNull(PermitTable.approvalNote),
                createdAt = row[PermitTable.createdAt].toString(),
                updatedAt = row[PermitTable.updatedAt].toString(),
                approvedAt = row.getOrNull(PermitTable.approvedAt)?.toString()
            )
        }
    }
}
