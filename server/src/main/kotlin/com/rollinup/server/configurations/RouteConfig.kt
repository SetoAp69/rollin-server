package com.rollinup.server.configurations

import JwtAuthClaim
import com.rollinup.server.route.Route
import com.rollinup.server.util.getAuthClaim
import io.ktor.server.application.Application
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        route(
            path = Route.Auth.path
        ) {
            Route.Auth.route(this)
        }
        route(
            path = Route.User.path
        ) {
            Route.User.route(this)
        }
        route(
            path = Route.Test.path
        ) {
            Route.Test.route(this)
        }
    }
}

