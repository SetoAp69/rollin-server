package com.rollinup.server.service.holiday

import com.rollinup.server.datasource.database.repository.holiday.HolidayRepository
import com.rollinup.server.mapper.HolidayMapper
import com.rollinup.server.model.request.holiday.CreateHolidayBody
import com.rollinup.server.model.request.holiday.EditHolidayBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.holiday.GetHolidayListResponse
import com.rollinup.server.util.Utils.toLocalDate
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.successDeleteResponse
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse

class HolidayServiceImpl(
    private val transactionManager: TransactionManager,
    private val holidayRepository: HolidayRepository,
    private val mapper: HolidayMapper,
) : HolidayService {
    override suspend fun getHolidayList(range: List<Long>?): Response<GetHolidayListResponse> =
        transactionManager.suspendTransaction {
            val data = holidayRepository.getHolidayList(range?.map { it.toLocalDate() })
            val response = Response(
                status = 200,
                message = "holiday".successGettingResponse(),
                data = mapper.mapGetHolidayResponse(data)
            )

            return@suspendTransaction response
        }

    override suspend fun createHoliday(body: CreateHolidayBody): Response<Unit> =
        transactionManager.suspendTransaction {

            holidayRepository.createHoliday(body)

            return@suspendTransaction Response(
                status = 201,
                message = "holiday".successCreateResponse()
            )
        }

    override suspend fun editHoliday(
        id: String,
        body: EditHolidayBody,
    ): Response<Unit> =
        transactionManager.suspendTransaction {
            holidayRepository.getHolidayList(id = id).ifEmpty {
                throw "holiday".notFoundException()
            }

            holidayRepository.editHoliday(id, body)

            return@suspendTransaction Response(
                status = 201,
                message = "holiday".successEditResponse()
            )
        }

    override suspend fun deleteHoliday(listId: List<String>): Response<Unit> =
        transactionManager.suspendTransaction {
            holidayRepository.deleteHoliday(listId)
            return@suspendTransaction Response(
                status = 201,
                message = "holiday".successDeleteResponse()
            )
        }
}