package com.rollinup.server.route.auth

import com.rollinup.server.ExpiredTokenExceptions
import com.rollinup.server.model.auth.LoginRequest
import com.rollinup.server.model.request.user.RefreshTokenRequest
import com.rollinup.server.service.auth.AuthService
import com.rollinup.server.service.refreshtoken.RefreshTokenService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import org.apache.commons.logging.Log
import org.koin.core.logger.Logger
import org.koin.ktor.ext.inject

fun Route.authRoute() {
    val authService by inject<AuthService>()
    val refreshTokenService by inject<RefreshTokenService>()

    authenticate("keycloakOAuth") {
        get("/login-keycloak") {
            val principal: OAuthAccessTokenResponse.OAuth2? = call.principal()
            call.respondText("${principal?.accessToken}")
        }
        get("/") {

        }
    }
    authenticate("auth-jwt") {
        get("/auth") {
            val principal = call.principal<JWTPrincipal>()
            val username = principal!!.payload.getClaim("id").asString()
            val expiresAt = principal.expiresAt?.time?.minus(System.currentTimeMillis())
            call.respondText("Hello, $username! Token is expired at $expiresAt ms.")
        }
    }


    post("/login") {
        try {
            val loginRequest = call.receive<LoginRequest>()

            val response = authService.basicAuth(
                authRequest = loginRequest
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = response
            )

        } catch (e: IllegalStateException) {
            call.respond(HttpStatusCode.BadRequest, message = e.message?:"")
        } catch (e: Exception) {
            call.respond(
                HttpStatusCode(
                    value = 403,
                    description = e.message ?: ""
                )
            )
        }
    }


    route("/refresh-token") {
        post {
            try {
                val refreshToken = call.receive<RefreshTokenRequest>().refreshToken

                if (refreshToken.isBlank()) {
                    call.respond(HttpStatusCode.BadRequest)
                } else {
                    val response = refreshTokenService.refreshToken(refreshToken)

                    call.respond(HttpStatusCode.OK, response)
                }
            } catch (e: ExpiredTokenExceptions) {
                call.respond(
                    HttpStatusCode(
                        value = 403,
                        description = e.message ?: ""
                    )
                )
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

    authenticate("auth-jwt") {
        get("/auth") {
            val id = call.principal<JWTPrincipal>()?.payload?.getClaim("id")?.asString()

            if (id.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val response = authService.getUserById(id)
            call.respond(
                status = HttpStatusCode.OK,
                message = response
            )
        }
    }

}