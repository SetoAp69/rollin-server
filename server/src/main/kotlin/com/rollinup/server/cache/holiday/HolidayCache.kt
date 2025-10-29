package com.rollinup.server.cache.holiday

import com.rollinup.server.InvalidCacheException
import com.rollinup.server.datasource.database.model.holiday.Holiday
import java.time.LocalDate

class HolidayCache {
    @Volatile
    var current: List<Holiday>? = null

    fun update(updatedList: List<Holiday>) {
        current = updatedList
    }

    fun get(): List<LocalDate> = current?.map { it.date }
        ?: throw InvalidCacheException()
}