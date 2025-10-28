package com.rollinup.server.generalsetting

import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import kotlinx.coroutines.flow.MutableSharedFlow

class GeneralSettingEventBus {
    val events = MutableSharedFlow<GeneralSetting>(extraBufferCapacity = 64)

    suspend fun emit(event: GeneralSetting) {
        events.emit(event)
    }
}