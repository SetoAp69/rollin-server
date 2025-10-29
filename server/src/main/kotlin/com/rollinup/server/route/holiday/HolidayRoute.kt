package com.rollinup.server.route.holiday

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.ListIdBody
import com.rollinup.server.model.request.holiday.CreateHolidayBody
import com.rollinup.server.model.request.holiday.EditHolidayBody
import com.rollinup.server.service.holiday.HolidayService
import com.rollinup.server.util.Utils
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.holidayRoute() {
    val service by inject<HolidayService>()

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.STUDENT, Role.TEACHER) {
            get {
                val dateRange = Utils.decodeJsonList<Long>(call.queryParameters["dateRange"])
                val response = service.getHolidayList(dateRange)

                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            post {
                val body = call.receive<CreateHolidayBody>()
                val response = service.createHoliday(body)

                call.respond(status = response.statusCode, message = response.message)
            }
        }
    }


    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            put("/{id}") {
                val id = call.pathParameters["id"]
                    ?.let {
                        it.ifBlank { null }
                    }
                    ?: throw IllegalPathParameterException("id")

                val body = call.receive<EditHolidayBody>()
                val response = service.editHoliday(id, body)

                call.respond(status = response.statusCode, message = response.message)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            put("/delete") {
                val listId = call.receive<ListIdBody>().listId
                val response = service.deleteHoliday(listId)

                call.respond(status = response.statusCode, message = response.message)
            }
        }
    }
}