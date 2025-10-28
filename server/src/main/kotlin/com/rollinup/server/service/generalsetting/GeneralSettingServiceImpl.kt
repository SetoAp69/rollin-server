package com.rollinup.server.service.generalsetting

import com.rollinup.server.datasource.database.repository.generalsetting.GeneralSettingRepository
import com.rollinup.server.mapper.GeneralSettingMapper
import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.generalsetting.GetGeneralSettingResponse
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse

class GeneralSettingServiceImpl(
    private val generalSettingRepository: GeneralSettingRepository,
    private val transactionManager: TransactionManager,
    private val mapper: GeneralSettingMapper,
) : GeneralSettingService {
    override suspend fun getGeneralSetting(): Response<GetGeneralSettingResponse> =
        transactionManager.suspendTransaction {
            val data = generalSettingRepository.getGeneralSetting()
                ?: throw "general setting".notFoundException()

            val result = mapper.mapGetGeneralSettingResponse(data)
            val response = Response(
                status = 200,
                message = "general setting".successGettingResponse(),
                data = result
            )
            return@suspendTransaction response
        }

    override suspend fun updateGeneralSetting(
        body: EditGeneralSettingBody,
        editBy: String,
    ): Response<Unit> =
        transactionManager.suspendTransaction {

            generalSettingRepository.updateGeneralSetting(body, editBy)

            return@suspendTransaction Response(
                status = 201,
                message = "general setting".successEditResponse()
            )
        }
}