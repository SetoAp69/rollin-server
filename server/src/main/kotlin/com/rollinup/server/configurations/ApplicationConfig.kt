package com.rollinup.server.configurations

import com.rollinup.server.di.DataSourceModule
import com.rollinup.server.di.DomainModule
import com.rollinup.server.di.RepositoryModule
import com.rollinup.server.di.appModule
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import org.koin.ktor.plugin.Koin

fun Application.module() {

    install(Koin) {
        modules(
            modules = listOf(
                appModule,
                DataSourceModule.module,
                RepositoryModule.module,
                DomainModule.module
            )

        )
    }

    install(ContentNegotiation) {
        json()
    }
    configureAuthentication()
    configureDatabase()
    configureRouting()
}