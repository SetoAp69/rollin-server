package com.rollinup.server.datasource.database.model.permit


import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.datasource.database.model.PermitType
import org.jetbrains.exposed.v1.core.ResultRow

data class PermitListEntity(
    val id: String = "",
    val name: String = "",
    val date: String = "",
    val student: User = User(),
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVAL_PENDING,
    val type: PermitType = PermitType.DISPENSATION,
    val attachment: String = "",
    val reason: String? = null,
    val permitStart: String = "",
    val permitEnd: String = "",
    val createdAt: String = "",
    val updatedAt: String = "",
) {
    data class User(
        val id: String = "",
        val name: String = "",
        val username: String = "",
        val classX: String = "",
    )

    companion object {
        fun fromResultRow(
            row: ResultRow,
        ): PermitListEntity {
            return PermitListEntity(
                id = row[PermitTable._id].toString(),
                name = row[PermitTable.name],
                date = row[PermitTable.createdAt].toLocalDate().toString(),
                student = User(
                    id = row[UserTable.user_id].toString(),
                    name = row[UserTable.firstName] + " " + row[UserTable.firstName],
                    username = row[UserTable.username],
                    classX = row[ClassTable.name]
                ),
                attachment = row[PermitTable.attachment],
                approvalStatus = row[PermitTable.approvalStatus],
                type = row[PermitTable.type],
                reason = row[PermitTable.reason],
                permitStart = row[PermitTable.startTime].toString(),
                permitEnd = row[PermitTable.endTime].toString(),
                createdAt = row[PermitTable.createdAt].toString(),
                updatedAt = row[PermitTable.updatedAt].toString(),
            )
        }
    }
}
