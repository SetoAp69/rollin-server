package com.rollinup.server.di

import com.rollinup.server.datasource.apiservice.repository.RegistrationRepository
import com.rollinup.server.datasource.apiservice.repository.RegistrationRepositoryImpl
import org.koin.dsl.module

object RepositoryModule {
    val module = module {
        single<RegistrationRepository> {
            RegistrationRepositoryImpl(
                registrationApiDataSource = get(),
                registrationMapper = get(),
                ioDispatcher = get()
            )
        }
    }
}