package com.rollinup.server.route

import com.rollinup.server.model.request.user.RegisterEditUserRequest
import com.rollinup.server.service.user.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.userRoute() {

    val userService by inject<UserService>()

    post("/register") {

        /*TODO add RBAC, only admin allowed to modify users account */
        try {
            val registerRequest = call.receive<RegisterEditUserRequest>()
            userService.registerUser(registerRequest)
            call.respond(HttpStatusCode.Created.copy(description = "success, user successfully registered"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest.copy(description = e.message ?: ""))
        }
    }

    put("/{id}/edit") {
        val id = call.parameters["id"]
        val editRequest = call.receive<RegisterEditUserRequest>()

        if (id.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest)
            return@put
        }

        try {
            userService.editUser(editRequest, id)
            call.respond(HttpStatusCode.Created.copy(description = "success, user data successfully edited"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest.copy(description = e.message ?: ""))
        }
    }

    put("reset-password/{token}") {
        val token = call.parameters["token"]

        if (token.isNullOrBlank()) {
            call.respond(HttpStatusCode.BadRequest.copy(description = "token is null or blank"))
            return@put
        }

        try {
//            userService.resetPassword()
            call.respond(HttpStatusCode.Created.copy(description = "success, user data successfully edited"))
        } catch (e: Exception) {
            call.respond(HttpStatusCode.BadRequest.copy(description = e.message ?: ""))
        }
    }
}