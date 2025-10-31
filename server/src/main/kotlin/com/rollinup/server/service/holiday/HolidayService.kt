package com.rollinup.server.service.holiday

import com.rollinup.server.model.request.holiday.CreateHolidayBody
import com.rollinup.server.model.request.holiday.EditHolidayBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.holiday.GetHolidayListResponse

interface HolidayService{
    suspend fun getHolidayList(range:List<Long>?): Response<GetHolidayListResponse>

    suspend fun createHoliday(body: CreateHolidayBody):Response<Unit>

    suspend fun editHoliday(id:String, body: EditHolidayBody):Response<Unit>

    suspend fun deleteHoliday(listId:List<String>):Response<Unit>
}
