package com.rollinup.server.configurations

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.rollinup.server.UnauthorizedTokenException
import com.rollinup.server.util.Config
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt

fun Application.configureAuthentication() {
    val config = Config.getTokenConfig()

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(
                verifier = JWT
                    .require(Algorithm.HMAC256(config.secret))
                    .withAudience(config.audience)
                    .withIssuer(config.issuer)
                    .build()
            )

            validate { cred ->
                JWTPrincipal(cred.payload)
            }
            challenge { defaultScheme, realm ->
                throw UnauthorizedTokenException()
            }
        }
    }


}