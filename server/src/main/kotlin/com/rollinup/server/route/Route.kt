package com.rollinup.server.route

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
}