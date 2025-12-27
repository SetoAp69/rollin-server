package com.rollinup.server.route.dashboard

import com.rollinup.server.service.dashboard.DashboardService
import com.rollinup.server.util.missingArgumentException
import com.rollinup.server.util.withClaim
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import java.time.LocalDate


fun Route.dashboardRoute() {
    val dashboardService by inject<DashboardService>()
    authenticate("auth-jwt") {
        get {
            withClaim { claim ->
                val role = claim.role
                val id = claim.id
                val date = call.parameters["date"]?.let { LocalDate.parse(it) }
                    ?:throw "date".missingArgumentException()

                val response = dashboardService.getDashboardData(id, role, date)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }
}