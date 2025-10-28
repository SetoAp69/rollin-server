package com.rollinup.server.route.permit

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.ListIdBody
import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.model.request.permit.PermitApprovalBody
import com.rollinup.server.service.permit.PermitService
import com.rollinup.server.util.Utils
import com.rollinup.server.util.withClaim
import io.ktor.http.content.MultiPartData
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.permitRoute() {
    val service by inject<PermitService>()
    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.ADMIN) {
            get("/by-student/{studentId}") {
                val queryParams = GetPermitQueryParams(
                    limit = call.queryParameters["limit"]?.toIntOrNull(),
                    page = call.queryParameters["page"]?.toIntOrNull(),
                    sortBy = call.queryParameters["sortBy"],
                    order = call.queryParameters["order"],
                    search = call.queryParameters["search"],
                    listId = Utils.decodeJsonList(call.queryParameters["listId"]),
                    isActive = call.queryParameters["isActive"]?.toBoolean() ?: true,
                    type = Utils.decodeJsonList(call.queryParameters["type"]),
                    dateRange = Utils.decodeJsonList(call.queryParameters["dateRange"]),
                    date = call.queryParameters["date"]?.toLong(),
                    status = Utils.decodeJsonList(call.queryParameters["status"])
                )


                val id = call.pathParameters["studentId"]?.let {
                    it.ifBlank { null }
                } ?: throw IllegalPathParameterException("studentId")

                val response = service.getPermitByStudent(
                    studentId = id,
                    queryParams = queryParams
                )

                call.respond(status = response.statusCode, message = response)
            }
        }
    }
    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.ADMIN) {
            get("/by-class/{classKey}") {
                val queryParams = GetPermitQueryParams(
                    limit = call.queryParameters["limit"]?.toIntOrNull(),
                    page = call.queryParameters["page"]?.toIntOrNull(),
                    sortBy = call.queryParameters["sortBy"],
                    order = call.queryParameters["order"],
                    search = call.queryParameters["search"],
                    listId = Utils.decodeJsonList(call.queryParameters["listId"]),
                    isActive = call.queryParameters["isActive"]?.toBoolean() ?: true,
                    type = Utils.decodeJsonList(call.queryParameters["type"]),
                    dateRange = Utils.decodeJsonList(call.queryParameters["dateRange"]),
                    date = call.queryParameters["date"]?.toLong(),
                    status = Utils.decodeJsonList(call.queryParameters["status"])
                )

                val classKey = call.pathParameters["classKey"]?.toIntOrNull()
                    ?: throw IllegalPathParameterException("classKey")

                val response = service.getPermitByClass(
                    queryParams = queryParams,
                    classKey = classKey,
                )

                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.STUDENT, Role.TEACHER, Role.ADMIN) {
            get("/{id}") {
                val id = call.pathParameters["id"]?.let {
                    it.ifBlank { null }
                } ?: throw IllegalPathParameterException("id")

                val response = service.getPermitById(id)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.ADMIN) {
            post("/approval") {
                withClaim { claim ->
                    val body = call.receive<PermitApprovalBody>()
                    val response = service.doApproval(approverId = claim.id, body = body)
                    call.respond(status = response.statusCode, message = response)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.ADMIN, Role.STUDENT) {
            post {
                val multiPart = call.receive<MultiPartData>()
                val response = service.createPermit(multiPart = multiPart)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.ADMIN, Role.STUDENT) {
            put("/{id}") {
                val id = call.pathParameters["id"]?.let {
                    it.ifBlank { null }
                } ?: throw IllegalPathParameterException("id")

                val multiPart = call.receive<MultiPartData>()
                val response = service.editPermit(id = id, multiPart = multiPart)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.STUDENT, Role.ADMIN) {
            put("/cancel") {
                val body = call.receive<ListIdBody>()
                val response = service.cancelPermit(body.listId)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            put("/delete") {
                val body = call.receive<ListIdBody>()
                val response = service.deletePermit(body.listId)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }

}