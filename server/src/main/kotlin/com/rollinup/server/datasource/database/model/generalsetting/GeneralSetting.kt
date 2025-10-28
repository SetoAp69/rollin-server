package com.rollinup.server.datasource.database.model.generalsetting

import com.rollinup.server.datasource.database.table.GeneralSettingTable
import com.rollinup.server.datasource.database.table.UserTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.LocalTime
import java.time.OffsetDateTime

data class GeneralSetting(
    val semesterStart: OffsetDateTime,
    val semesterEnd: OffsetDateTime,
    val checkInPeriodStart: LocalTime,
    val checkInPeriodEnd: LocalTime,
    val schoolPeriodStart: LocalTime,
    val schoolPeriodEnd: LocalTime,
    val rad: Double,
    val long: Double,
    val lat: Double,
    val updatedAt: LocalTime,
    val modifiedBy: String,
    val modifiedByName: String,
) {
    companion object {
        fun fromRow(row: ResultRow) = GeneralSetting(
            semesterStart = row[GeneralSettingTable.semesterStart],
            semesterEnd = row[GeneralSettingTable.semesterEnd],
            checkInPeriodStart = row[GeneralSettingTable.checkInPeriodStart],
            checkInPeriodEnd = row[GeneralSettingTable.checkInPeriodEnd],
            schoolPeriodStart = row[GeneralSettingTable.schoolPeriodStart],
            schoolPeriodEnd = row[GeneralSettingTable.schoolPeriodEnd],
            rad = row[GeneralSettingTable.geofenceRadius],
            long = row[GeneralSettingTable.longitude],
            lat = row[GeneralSettingTable.latitude],
            updatedAt = row[GeneralSettingTable.updatedAt],
            modifiedBy = row[GeneralSettingTable.modifiedBy].toString(),
            modifiedByName = row[UserTable.firstName] + " " + row[UserTable.lastName]
        )
    }
}