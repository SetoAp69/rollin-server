package com.rollinup.server.route

import com.rollinup.server.route.auth.authRoute
import com.rollinup.server.route.test.testRoute
import com.rollinup.server.route.user.userRouteNew

sealed class Route(
    val path: String,
    val route: io.ktor.server.routing.Route.() -> Unit
) {
    object Auth : Route(
        path = "/auth",
        route = {
            authRoute()
        }
    )

    object User : Route(
        path = "/user",
        route = {
            userRouteNew()
        }
    )

    object Test : Route(
        path = "/test",
        route = {
            testRoute()
        }
    )
}