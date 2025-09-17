package com.rollinup.server.di

import com.rollinup.server.datasource.apiservice.domain.registration.RegistrationDomainModule
import org.koin.dsl.module

object DomainModule {
    val module = module {
        includes(
            RegistrationDomainModule.module
        )
    }
}

