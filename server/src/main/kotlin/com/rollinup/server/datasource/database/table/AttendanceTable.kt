package com.rollinup.server.datasource.database.table

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.PGEnum
import com.rollinup.server.util.Utils
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import java.time.OffsetDateTime
import java.util.UUID

object AttendanceTable : Table("attendance") {
    val _id = uuid("_id").clientDefault { UUID.randomUUID() }
    val userId = reference(
        name = "user_id",
        refColumn = UserTable.user_id,
        fkName = "fkey_attendance_user",
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )

    val status: Column<AttendanceStatus> = customEnumeration(
        name = "status",
        fromDb = { status -> AttendanceStatus.fromValue(status as String) },
        toDb = { status -> PGEnum("status", status) }
    )
    val permit = reference(
        name = "permit",
        refColumn = PermitTable._id,
        fkName = "fk_attendance_permit",
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    ).nullable()

    val createdAt =
        timestampWithTimeZone(name = "created_at").clientDefault { OffsetDateTime.now(Utils.getOffset()) }
    val updatedAt =
        timestampWithTimeZone(name = "updated_at").clientDefault { OffsetDateTime.now(Utils.getOffset()) }
    val checkedInAt = timestampWithTimeZone(name = "checked_in_at").nullable()
    val date = date("date")
    val attachment = varchar("attachment", 256).nullable()
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()
}