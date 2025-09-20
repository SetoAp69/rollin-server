package com.rollinup.server.route

import com.rollinup.server.route.auth.authRoute
import com.rollinup.server.route.tasks.taskRoute

sealed class Route(
    val path: String,
    val route: io.ktor.server.routing.Route.() -> Unit
) {
    object Tasks : Route(
        path = "/tasks",
        route = {
            taskRoute()
        }
    )

    object Auth:Route(
        path = "/auth",
        route = {
            authRoute()
        }
    )

    object User:Route(
        path = "/user",
        route = {
            userRoute()
        }
    )
}