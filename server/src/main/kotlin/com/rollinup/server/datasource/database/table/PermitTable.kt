package com.rollinup.server.datasource.database.table

import com.rollinup.server.datasource.database.model.user.PGEnum
import com.rollinup.server.model.ApprovalStatus
import com.rollinup.server.model.PermitType
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object PermitTable : Table("permit") {
    val _id = uuid("_id")
    val user_id = reference(
        name = "user_id",
        refColumn = UserTable.user_id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
        fkName = "fk_permit_user"
    )

    val type = customEnumeration(
        name = "permit_type",
        fromDb = { type -> PermitType.fromValue(type as String) },
        toDb = { type -> PGEnum("permit_type", type) }
    )
    val reason = varchar("reason", 30)
    val note = varchar("note", 120).nullable()
    val startTime = timestampWithTimeZone("start_time")
    val endTime = timestampWithTimeZone("end_time")
    val attachment = varchar("attachment", 256)

    val approvalStatus = customEnumeration(
        name = "approval_status",
        fromDb = { status -> ApprovalStatus.fromValue(status as String) },
        toDb = { status -> PGEnum("approval_status", status) }
    )
    val approvedBy = reference(
        name = "approved_by",
        refColumn = UserTable.user_id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
        fkName = "fk_permit_approved_by"
    ).nullable()
    val approvedAt = timestampWithTimeZone("approved_at").nullable()
    val approvalNote = varchar("approval_note", 120).nullable()
    val createdAt = timestampWithTimeZone("created_at")
    val updatedAt = timestampWithTimeZone("updated_at")

    val sortField
        get() = mapOf(
            "reason" to reason,
            "approvalStatus" to approvalStatus,
            "type" to type
        )
}