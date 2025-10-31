package com.rollinup.server.configurations

import com.rollinup.server.route.Route
import io.ktor.server.application.Application
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
            path = Route.Attendance.path
        ) {
            Route.Attendance.route(this)
        }

        route(
            path = Route.File.path
        ) {
            Route.File.route(this)
        }

        route(
            path = Route.Permit.path
        ) {
            Route.Permit.route(this)
        }

        route(
            path = Route.GeneralSetting.path
        ) {
            Route.GeneralSetting.route(this)
        }

        route(
            path = Route.Holiday.path
        ) {
            Route.Holiday.route(this)
        }

    }
}

