package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.time
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object GeneralSettingTable : Table("general_setting") {
    val semesterStart = timestampWithTimeZone("semester_start")
    val semesterEnd = timestampWithTimeZone("semester_end")
    val modifiedBy = reference(
        name = "modified_by",
        refColumn = UserTable.user_id,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )
    val updatedAt = time("updated_at")
    val schoolPeriodStart = time("school_period_start")
    val schoolPeriodEnd = time("school_period_end")
    val checkInPeriodStart = time("check_in_period_start")
    val checkInPeriodEnd = time("check_in_period_end")
    val latitude = double("latitude")
    val longitude = double("longitude")
    val geofenceRadius = double("geofence_radius")
}