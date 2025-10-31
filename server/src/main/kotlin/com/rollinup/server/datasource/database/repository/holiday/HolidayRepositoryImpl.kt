package com.rollinup.server.datasource.database.repository.holiday

import com.rollinup.server.datasource.database.model.holiday.Holiday
import com.rollinup.server.datasource.database.table.HolidayTable
import com.rollinup.server.model.request.holiday.CreateHolidayBody
import com.rollinup.server.model.request.holiday.EditHolidayBody
import com.rollinup.server.util.Utils.toLocalDate
import com.rollinup.server.util.addFilter
import org.jetbrains.exposed.v1.core.statements.UpsertSqlExpressionBuilder.inList
import org.jetbrains.exposed.v1.jdbc.andWhere
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import java.time.LocalDate
import java.util.UUID

class HolidayRepositoryImpl() : HolidayRepository {
    override fun getHolidayList(range: List<LocalDate>?, id: String?): List<Holiday> {
        val query = HolidayTable
            .selectAll()

        query.addFilter(id) {
            if (it.isNotBlank()) {
                andWhere { HolidayTable._id eq UUID.fromString(it) }
            }
        }
        query.addFilter(range) {
            andWhere { HolidayTable.date.between(it.first(), it.last()) }
        }

        val result = query.map { row ->
            Holiday.fromResultRow(row)
        }

        return result
    }

    override fun createHoliday(body: CreateHolidayBody) {
        HolidayTable.insert { statement ->
            statement[name] = body.name
            statement[date] = body.date.toLocalDate()
        }
    }

    override fun editHoliday(id: String, body: EditHolidayBody) {
        HolidayTable.update(
            where = { HolidayTable._id eq UUID.fromString(id) }
        ) { statement ->
            body.name?.let {
                statement[name] = it
            }

            body.date?.let {
                statement[date] = it.toLocalDate()
            }
        }
    }

    override fun deleteHoliday(listId: List<String>) {
        HolidayTable.deleteWhere {
            this._id inList listId.map { UUID.fromString(it) }
        }
    }
}