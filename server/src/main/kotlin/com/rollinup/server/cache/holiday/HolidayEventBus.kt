package com.rollinup.server.cache.holiday

import com.rollinup.server.datasource.database.model.holiday.Holiday
import kotlinx.coroutines.flow.MutableSharedFlow

class HolidayEventBus {
    val events = MutableSharedFlow<List<Holiday>>(extraBufferCapacity = 64)

    suspend fun emit(event: List<Holiday>) {
        events.emit(event)
    }
}