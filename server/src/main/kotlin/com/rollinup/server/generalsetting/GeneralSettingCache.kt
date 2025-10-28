package com.rollinup.server.generalsetting

import com.rollinup.server.InvalidSettingException
import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting

class GeneralSettingCache {
    @Volatile
    var current: GeneralSetting? = null

    fun update(newSetting: GeneralSetting){
        current = newSetting
    }

    fun get(): GeneralSetting = current
        ?:throw InvalidSettingException()
}