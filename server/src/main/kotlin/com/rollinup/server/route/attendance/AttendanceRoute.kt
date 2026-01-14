package com.rollinup.server.route.attendance

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.CreateAttendanceRequest
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.request.attendance.GetExportAttendanceQueryParams
import com.rollinup.server.service.attendance.AttendanceService
import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.stringJsonToList
import com.rollinup.server.util.missingArgumentException
import com.rollinup.server.util.withClaim
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject
import java.io.File
import java.time.LocalDate

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
                        status = call.queryParameters["status"]?.stringJsonToList(),
                        dateRange = call.queryParameters["dateRange"]?.stringJsonToList()
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
        withRole(Role.ADMIN, Role.STUDENT, Role.TEACHER) {
            get("/by-student/{studentId}/summary") {
                val dateRange = call.queryParameters["dateRange"]?.stringJsonToList<Long>()
                val id = call.pathParameters["studentId"]
                    ?: throw IllegalPathParameterException("studentId")

                val response = attendanceService.getAttendanceListByStudentSummary(
                    dateRange = dateRange,
                    studentId = id
                )

                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER) {
            get("/by-class/{classKey}") {
                withClaim { claim ->
                    val classKey = call.pathParameters["classKey"]?.toIntOrNull()
                        ?: throw IllegalPathParameterException("classKey")

                    val queryParams = GetAttendanceByClassQueryParams(
                        search = call.queryParameters["search"],
                        limit = call.queryParameters["limit"]?.toIntOrNull(),
                        page = call.queryParameters["page"]?.toIntOrNull(),
                        date = call.queryParameters["date"]?.toLongOrNull(),
                        sortBy = call.queryParameters["sortBy"],
                        order = call.queryParameters["order"],
                        status = call.queryParameters["status"]?.stringJsonToList(),
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
        withRole(Role.ADMIN, Role.TEACHER) {
            get("/by-class/{classKey}/summary") {
                withClaim { claim ->
                    val classKey = call.pathParameters["classKey"]?.toIntOrNull()
                        ?: throw IllegalPathParameterException("studentId")
                    val date = call.queryParameters["date"]?.toLongOrNull()
                        ?: throw "date".missingArgumentException()

                    val response = attendanceService.getAttendanceListByClassSummary(
                        classKey = classKey,
                        date = date
                    )

                    call.respond(status = response.statusCode, message = response)
                }
            }

        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER, Role.STUDENT) {
            get("/{id}") {
                withClaim { claim->
                    val role = claim.role
                    val attendanceId = call.pathParameters["id"]?.let {
                        it.ifBlank { null }
                    } ?: throw IllegalPathParameterException("id")


                    val response = attendanceService.getAttendanceById(id = attendanceId,role = role)

                    call.respond(status = response.statusCode, message = response)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER) {
            post {
                val body = call.receive<CreateAttendanceRequest>()
                val response = attendanceService.createAttendanceData(body)

                call.respond(status = response.statusCode, message = response.message)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.STUDENT) {
            post("/check-in") {
                withClaim { claim ->
                    val id = claim.id
                    val multiPart = call.receiveMultipart()
                    val fileHashMap = hashMapOf<String, File>()
                    val formHashMap = hashMapOf<String, String>()

                    multiPart.forEachPart { partData ->
                        when (partData) {
                            is PartData.FileItem -> {
                                Utils.fetchFileData(
                                    partData = partData,
                                    hashMap = fileHashMap,
                                    customName = "attachment"
                                )
                            }

                            is PartData.FormItem -> {
                                Utils.fetchFormData(
                                    partData = partData,
                                    hashMap = formHashMap
                                )
                            }

                            else -> {}
                        }
                        partData.dispose()
                    }

                    val response = attendanceService.checkIn(id, formHashMap, fileHashMap)
                    call.respond(status = response.statusCode, message = response.message)

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
                    val formHashMap: HashMap<String, String> = hashMapOf()
                    val fileHashMap: HashMap<String, File> = hashMapOf()

                    multiPart.forEachPart { partData ->
                        when (partData) {
                            is PartData.FormItem -> Utils.fetchFormData(partData, formHashMap)
                            is PartData.FileItem -> Utils.fetchFileData(partData, fileHashMap)
                            else -> {}
                        }
                        partData.dispose()
                    }

                    val response = attendanceService.updateAttendance(
                        id = id,
                        editBy = claim.id,
                        formHashMap = formHashMap,
                        fileHashMap = fileHashMap,
                    )
                    call.respond(status = response.statusCode, message = response.message)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        get("/export") {
            val dateRange = call.queryParameters["dateRange"]?.stringJsonToList<String>()
                ?.map { LocalDate.parse(it) }
            val classKey = call.queryParameters["class"]?.toIntOrNull()
            val queryParams =
                GetExportAttendanceQueryParams(dateRange = dateRange, classKey = classKey)

            val response = attendanceService.getExportAttendanceData(queryParams)

            call.respond(status = response.statusCode, message = response)
        }
    }

}

