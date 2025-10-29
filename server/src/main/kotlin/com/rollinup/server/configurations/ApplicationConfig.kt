package com.rollinup.server.configurations

import com.rollinup.server.di.GeneralSettingModule
import com.rollinup.server.di.HolidayModule
import com.rollinup.server.di.MapperModule
import com.rollinup.server.di.RepositoryModule
import com.rollinup.server.di.ServiceModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.koin.ktor.plugin.Koin

fun Application.module() {

    install(Koin) {
        modules(
            modules = listOf(
                MapperModule.module,
                RepositoryModule.module,
                ServiceModule.module,
                GeneralSettingModule.module,
                HolidayModule.module,
            )
        )
    }

    install(ContentNegotiation) {
        json()
    }
    configureValidator()
    configureStatusPage()
    configureAuthentication()
    configureSSE()
    configureDatabase()
    configureListener()
    configureRouting()
}