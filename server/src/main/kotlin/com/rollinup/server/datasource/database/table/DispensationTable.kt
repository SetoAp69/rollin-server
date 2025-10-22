package com.rollinup.server.datasource.database.table

import com.rollinup.server.datasource.database.model.user.PGEnum
import com.rollinup.server.model.ApprovalStatus
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.time
import org.jetbrains.exposed.v1.javatime.timestamp

object DispensationTable : Table("absence") {
    val _id = uuid("_id")
    val attendanceId = reference(
        name = "attendance_id",
        refColumn = AttendanceTable._id,
        fkName = "fk_absence_attendance_id",
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val approvalStatus: Column<ApprovalStatus> = customEnumeration(
        name = "approval_status",
        fromDb = { status -> ApprovalStatus.fromValue(status as String) },
        toDb = { status -> PGEnum("approval_status", status) }
    )
    val note = varchar("note", 120)
    val approvalNote = varchar("approval_note", 120)
    val document = varchar("document", 120)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")
    val approvedAt = timestamp("approved_at")
    val approvedBy = reference(
        name = "approved_by",
        refColumn = UserTable.user_id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE,
        fkName = "fk_absence_approved_by"
    )
    val start_time = time("start_time")
    val end_time = time("end_time")

}