package com.rollinup.server.route.auth

import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.request.user.RefreshTokenRequest
import com.rollinup.server.service.auth.AuthService
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.authRoute() {
    val authService by inject<AuthService>()

    post("/login") {
        val request = call.receive<LoginRequest>()
        val response = authService.login(request)

        call.respond(
            status = response.statusCode,
            message = response
        )
    }

    post("/refresh-token") {
        val request = call.receive<RefreshTokenRequest>()
        val response = authService.refreshToken(request.refreshToken)

        call.respond(
            status = response.statusCode,
            message = response
        )
    }
}