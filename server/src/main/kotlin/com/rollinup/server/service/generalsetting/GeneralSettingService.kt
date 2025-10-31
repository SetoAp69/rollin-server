package com.rollinup.server.service.generalsetting

import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.generalsetting.GetGeneralSettingResponse

interface GeneralSettingService {
    suspend fun getGeneralSetting(): Response<GetGeneralSettingResponse>

    suspend fun updateGeneralSetting(body: EditGeneralSettingBody, editBy:String): Response<Unit>
}