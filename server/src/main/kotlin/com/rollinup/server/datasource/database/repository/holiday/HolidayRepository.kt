package com.rollinup.server.datasource.database.repository.holiday

import com.rollinup.server.datasource.database.model.holiday.Holiday
import com.rollinup.server.model.request.holiday.CreateHolidayBody
import com.rollinup.server.model.request.holiday.EditHolidayBody
import java.time.LocalDate

interface HolidayRepository {
    fun getHolidayList(range: List<LocalDate>? = null, id: String? = null): List<Holiday>

    fun createHoliday(body: CreateHolidayBody)

    fun editHoliday(id: String, body: EditHolidayBody)

    fun deleteHoliday(listId: List<String>)
}