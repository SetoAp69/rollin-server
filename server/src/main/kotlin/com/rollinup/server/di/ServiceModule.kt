package com.rollinup.server.di

import com.rollinup.server.service.auth.AuthService
import com.rollinup.server.service.auth.AuthServiceImpl
import com.rollinup.server.service.jwt.JWTService
import com.rollinup.server.service.jwt.TokenService
import com.rollinup.server.service.refreshtoken.RefreshTokenService
import com.rollinup.server.service.refreshtoken.RefreshTokenServiceImpl
import com.rollinup.server.service.security.HashingService
import com.rollinup.server.service.security.HashingServiceImpl
import com.rollinup.server.service.user.UserService
import com.rollinup.server.service.user.UserServiceImpl
import org.koin.dsl.module

object ServiceModule {
    val module = module {
        single<JWTService>{
            JWTService()
        }

        single<TokenService> {
            JWTService()
        }

        single<RefreshTokenService>{
            RefreshTokenServiceImpl(
                tokenService = get(),
                refreshTokenRepository = get()
            )
        }

        single<HashingService>{
            HashingServiceImpl()
        }

        single<RefreshTokenService>{
            RefreshTokenServiceImpl(
                tokenService = get(),
                refreshTokenRepository = get()
            )
        }

        single<AuthService>{
            AuthServiceImpl(
                hashingService = get(),
                refreshTokenService = get(),
                jwtService = get(),
                userRepository = get()
            )
        }

        single<UserService> {
            UserServiceImpl(
                hashingService = get(),
                tokenService = get(),
                userRepository = get()
            )
        }

    }
}