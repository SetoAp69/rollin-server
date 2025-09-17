package com.rollinup.server.configurations

import com.rollinup.server.route.tasks.taskRoute
import com.rollinup.server.route.Route
import io.ktor.server.application.Application
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        route(
            path = Route.Tasks.path
        ) {
            Route.Tasks.route(this)
        }
        Route.Auth.route(this)
    }


//    {
//        route(
//            path = Route.Auth.path
//        ){
//            Route.Auth.route(this)
//        }
//    }

}