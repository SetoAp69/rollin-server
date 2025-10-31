package com.rollinup.server.di

import com.rollinup.server.cache.holiday.HolidayCache
import com.rollinup.server.cache.holiday.HolidayEventBus
import com.rollinup.server.cache.holiday.HolidayListener
import org.koin.dsl.module

object HolidayModule {
    val module = module {
        single<HolidayCache> { HolidayCache() }

        single<HolidayListener> {
            HolidayListener(
                holidayCache = get(),
                transactionManager = get(),
                holidayRepository = get(),
                holidayEventBus = get()
            )
        }

        single<HolidayEventBus> { HolidayEventBus() }
    }
}