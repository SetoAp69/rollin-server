package com.rollinup.server.route.attendance

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.service.attendance.AttendanceService
import com.rollinup.server.util.Utils.decodeJsonList
import com.rollinup.server.util.withClaim
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.attendanceRoute() {
    val attendanceService by inject<AttendanceService>()


    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.STUDENT, Role.TEACHER) {
            get("/by-student/{studentId}") {
                withClaim { claim ->
                    val studentId = call.pathParameters["studentId"]?.let {
                        it.ifBlank { null }
                    } ?: throw IllegalPathParameterException("studentId")

                    if (claim.role == Role.STUDENT && claim.id != studentId)
                        throw IllegalPathParameterException("studentId")

                    val queryParams = GetAttendanceByStudentQueryParams(
                        search = call.queryParameters["search"],
                        limit = call.queryParameters["limit"]?.toIntOrNull(),
                        page = call.queryParameters["page"]?.toIntOrNull(),
                        dateRange = decodeJsonList(call.queryParameters["dateRange"])
                    )

                    val response = attendanceService.getAttendanceListByStudent(
                        queryParams = queryParams,
                        studentId = studentId
                    )
                    call.respond(status = response.statusCode, message = response)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER) {
            get("/by-class/{classKey}") {
                withClaim { claim ->
                    val classKey = call.pathParameters["classKey"]?.toIntOrNull()
                        ?: throw IllegalPathParameterException("studentId")

                    val queryParams = GetAttendanceByClassQueryParams(
                        search = call.queryParameters["search"],
                        limit = call.queryParameters["limit"]?.toIntOrNull(),
                        page = call.queryParameters["page"]?.toIntOrNull(),
                        date = call.queryParameters["date"]?.toLongOrNull(),
                        sortBy = call.queryParameters["sortBy"],
                        order = call.queryParameters["order"],
                        status = decodeJsonList(call.queryParameters["status"]),
                    )

                    val response = attendanceService.getAttendanceListByClass(
                        queryParams = queryParams,
                        classKey = classKey
                    )

                    call.respond(status = response.statusCode, message = response)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER, Role.STUDENT) {
            get("/{id}") {
                val attendanceId = call.pathParameters["id"]?.let {
                    it.ifBlank { null }
                } ?: throw IllegalPathParameterException("id")


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

                    val response = attendanceService.createAttendanceData(
                        multiPartData = multiPart,
                        studentUserId = studentUserId,
                        role = claim.role
                    )
                    call.respond(response.statusCode, response.message)
                }

            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER) {
            put("/{id}") {
                withClaim { claim ->

                    val id = call.pathParameters["id"]?.let {
                        it.ifBlank { null }
                    } ?: throw IllegalPathParameterException("id")

                    val multiPart = call.receiveMultipart()

                    val response = attendanceService.updateAttendance(
                        id = id,
                        editBy = claim.id,
                        multiPartData = multiPart
                    )
                    call.respond(status = response.statusCode, message = response.message)
                }
            }
        }

    }

}

