package com.rollinup.server.datasource.database.model.permit


import com.rollinup.server.datasource.database.table.ClassTable
import com.rollinup.server.datasource.database.table.PermitTable
import com.rollinup.server.datasource.database.table.UserTable
import com.rollinup.server.model.ApprovalStatus
import com.rollinup.server.model.PermitType
import org.jetbrains.exposed.v1.core.ResultRow

data class PermitListEntity(
    val id: String = "",
    val name: String = "",
    val user: User = User(),
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVAL_PENDING,
    val type: PermitType = PermitType.DISPENSATION,
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
                user = User(
                    id = row[UserTable.user_id].toString(),
                    name = row[UserTable.firstName] + " " + row[UserTable.firstName],
                    username = row[UserTable.username],
                    classX = row[ClassTable.name]
                ),
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
