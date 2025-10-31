package com.rollinup.server.datasource.database.repository.generalsetting

import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody

interface GeneralSettingRepository {
    fun getGeneralSetting(): GeneralSetting?

    fun updateGeneralSetting(body: EditGeneralSettingBody, editBy: String)
}