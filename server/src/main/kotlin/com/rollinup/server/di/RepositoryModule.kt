package com.rollinup.server.di

import com.rollinup.server.datasource.apiservice.repository.RegistrationRepository
import com.rollinup.server.datasource.apiservice.repository.RegistrationRepositoryImpl
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepository
import com.rollinup.server.datasource.database.repository.refreshtoken.RefreshTokenRepositoryImpl
import com.rollinup.server.datasource.database.repository.user.UserRepository
import com.rollinup.server.datasource.database.repository.user.UserRepositoryImpl
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

        single<UserRepository> {
            UserRepositoryImpl(
                dao = get()
            )
        }

        single<RefreshTokenRepository> {
            RefreshTokenRepositoryImpl(
                refreshTokenDao = get()
            )
        }
    }
}