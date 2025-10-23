package com.rollinup.server.util.generalsetting

import com.rollinup.server.InvalidSettingException

class GeneralSettingCache {
    @Volatile
    var current: GeneralSetting? = null

    fun update(newSetting:GeneralSetting){
        current = newSetting
    }

    fun get():GeneralSetting = current
        ?:throw InvalidSettingException()
}
