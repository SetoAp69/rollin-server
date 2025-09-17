package com.rollinup.server.datasource.apiservice.domain.registration

import org.koin.dsl.module

object RegistrationDomainModule {
    val module = module {
        single {
            GetAdminAccessTokenUseCase(
                repository = get()
            )
        }

        single {
            GetRegistrationAccessTokenUseCase(
                repository = get()
            )
        }
    }
}