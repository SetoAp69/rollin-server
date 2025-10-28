package com.rollinup.server.configurations

import com.rollinup.server.generalsetting.GeneralSettingListener
import io.ktor.server.application.Application
import org.koin.ktor.ext.inject

fun Application.configureListener() {
    val generalSettingListener by inject<GeneralSettingListener>()
    generalSettingListener.startListening()
}
