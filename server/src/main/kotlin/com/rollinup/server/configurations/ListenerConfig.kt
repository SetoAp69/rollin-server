package com.rollinup.server.configurations

import com.rollinup.server.cache.generalsetting.GeneralSettingListener
import com.rollinup.server.cache.holiday.HolidayListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject

fun Application.configureListener() {
    val generalSettingListener by inject<GeneralSettingListener>()
    val holidayListener by inject<HolidayListener>()

    generalSettingListener.startListening()
    holidayListener.startListening()
}
