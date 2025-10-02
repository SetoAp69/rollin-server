package com.rollinup.server.di

import com.rollinup.server.mapper.AuthMapper
import com.rollinup.server.mapper.UserMapper
import com.rollinup.server.service.auth.AuthService
import com.rollinup.server.service.auth.AuthServiceImpl
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.service.email.EmailServiceImpl
import com.rollinup.server.service.jwt.JWTService
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.HashingServiceImpl
import com.rollinup.server.service.user.UserService
import com.rollinup.server.service.user.UserServiceImpl
import org.koin.dsl.module

object ServiceModule {
    val module = module {

        single<UserMapper> {
            UserMapper()
        }
        single { AuthMapper() }


        single<TokenService> {
            JWTService()
        }

        single<HashingService> {
            HashingServiceImpl()
        }

        single<EmailService> {
            EmailServiceImpl()
        }

        single<UserService> {
            UserServiceImpl(
                userRepository = get(),
                resetPasswordRepository = get(),
                hashingService = get(),
                tokenService = get(),
                emailService = get(),
                mapper = get()
            )
        }

        single<AuthService> {
            AuthServiceImpl(
                hashingService = get(),
                jwtService = get(),
                userRepository = get(),
                refreshTokenRepository = get(),
                authMapper = get(),
            )
        }


    }
}