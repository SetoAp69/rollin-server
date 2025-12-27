package com.rollinup.server.route.auth

import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.request.user.RefreshTokenRequest
import com.rollinup.server.model.request.user.UpdatePasswordAndDeviceRequest
import com.rollinup.server.service.auth.AuthService
import com.rollinup.server.service.user.UserService
import com.rollinup.server.util.withClaim
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.authRoute() {
    val authService by inject<AuthService>()
    val userService by inject<UserService>()

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

    authenticate("auth-jwt") {
        get("/login") {
            withClaim { claim ->
                val id = claim.id
                val response = authService.loginJWT(id)

                call.respond(
                    status = response.statusCode,
                    message = response
                )
            }
        }
    }
}