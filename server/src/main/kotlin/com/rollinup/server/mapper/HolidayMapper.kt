package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.holiday.Holiday
import com.rollinup.server.model.response.holiday.GetHolidayListResponse

class HolidayMapper {
    fun mapGetHolidayResponse(data:List<Holiday>) = GetHolidayListResponse(
        record = data.size,
        data = data.map {
            GetHolidayListResponse.Holiday(
                id = it.id,
                name = it.name,
                date = it.date.toString()
            )
        }
    )
}