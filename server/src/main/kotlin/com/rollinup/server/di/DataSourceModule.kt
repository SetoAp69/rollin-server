package com.rollinup.server.di

import com.rollinup.server.datasource.apiservice.datasource.RegistrationApi
import com.rollinup.server.datasource.apiservice.datasource.RegistrationApiDataSource
import org.koin.dsl.module

object DataSourceModule {
    val module = module {
        single<RegistrationApi> {
            RegistrationApiDataSource(
                client = get(),
                config = get(),
            )
        }
    }
}