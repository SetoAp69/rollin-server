package com.rollinup.server.route.attendance

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.service.attendance.AttendanceService
import com.rollinup.server.util.Utils.decodeJsonList
import com.rollinup.server.util.withClaim
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.attendanceRoute() {
    val attendanceService by inject<AttendanceService>()

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER, Role.STUDENT) {
            get {
                withClaim {
                    val queryParams = AttendanceQueryParams(
                        limit = call.queryParameters["limit"]?.toIntOrNull(),
                        page = call.queryParameters["page"]?.toIntOrNull(),
                        sortBy = call.queryParameters["sortBy"],
                        order = call.queryParameters["order"],
                        search = call.queryParameters["search"],
                        status = decodeJsonList(call.queryParameters["status"]),
                        xClass = decodeJsonList(call.queryParameters["class"]),
                        dateRange = decodeJsonList(call.queryParameters["dateRange"]),
                        studentId = call.queryParameters["studentId"]
                    )
                    val response = attendanceService.getAttendance(queryParams = queryParams)
                    call.respond(status = response.statusCode, message = response)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER, Role.STUDENT) {
            get("/{id}") {
                val attendanceId = call.pathParameters["id"]
                    ?.ifBlank { throw IllegalPathParameterException("id") }
                    ?: throw IllegalPathParameterException("id")


                val response = attendanceService.getAttendanceById(id = attendanceId)

                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER, Role.STUDENT) {
            post("/check-in") {
                withClaim { claim ->
                    val studentUserId = claim.id
                    val multiPart = call.receiveMultipart()

                    attendanceService.createAttendanceData(
                        multiPartData = multiPart,
                        studentUserId = studentUserId
                    )
                }
            }
        }

    }

}

