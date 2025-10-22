package com.rollinup.server.datasource.database.table

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.user.PGEnum
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZoneParam

object AttendanceTable : Table("attendance") {
    val _id = uuid("_id")
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

    val createdAt = timestampWithTimeZone(name = "created_at")
    val updatedAt = timestampWithTimeZone(name = "updated_at")
    val checkedInAt =timestampWithTimeZone(name = "checked_in_at")
    val date = date("date")
    val attachment = varchar("attachment", 256).nullable()
    val latitude = double("latitude").nullable()
    val longitude = double("longitude").nullable()

    val filterField
        get() = mapOf(
            "status" to status,
            "date" to date,
            "checkedInAt" to checkedInAt,
        )

    val sortField
        get() = mapOf(
            "checkIn" to checkedInAt,
            "date" to date,
        )
}