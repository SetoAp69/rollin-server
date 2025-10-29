package com.rollinup.server.datasource.database.model.holiday

import com.rollinup.server.datasource.database.table.HolidayTable
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.LocalDate

data class Holiday(
    val id: String,
    val name: String,
    val date: LocalDate,
) {
    companion object {
        fun fromResultRow(row: ResultRow) = Holiday(
            id = row[HolidayTable._id].toString(),
            name = row[HolidayTable.name],
            date = row[HolidayTable.date]
        )
    }
}
