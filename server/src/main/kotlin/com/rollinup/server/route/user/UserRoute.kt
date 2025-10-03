package com.rollinup.server.route.user

import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.ResetPasswordRequest
import com.rollinup.server.model.request.user.ResetPasswordRequestRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.request.user.ValidateOtpRequest
import com.rollinup.server.service.user.UserService
import com.rollinup.server.util.withClaim
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.userRouteNew() {
    val userService by inject<UserService>()
    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            post("/register") {
                val request = call.receive<RegisterUserRequest>()
                val response = userService.registerUser(request)

                call.respond(
                    status = response.statusCode,
                    message = response.message
                )
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.TEACHER) {
            get {
                withClaim { claim ->
                    val queryParams = UserQueryParams(
                        search = call.queryParameters["search"],
                        page = call.queryParameters["page"]?.toIntOrNull(),
                        limit = call.queryParameters["limit"]?.toIntOrNull(),
                        sortBy = call.queryParameters["sortBy"],
                        sortOrder = call.queryParameters["sortOrder"],
                        gender = call.queryParameters["gender"]?.split(","),
                        role = call.queryParameters["role"]?.split(",")
                    )
                    val response = userService.getAllUser(queryParams)

                    call.respond(
                        status = response.statusCode,
                        message = response
                    )
                }
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            put("/{id}/edit") {
                val id = call.pathParameters["id"]
                    ?: throw IllegalArgumentException("id")

                val editRequest = call.receive<EditUserRequest>()
                val response = userService.editUser(
                    requestBody = editRequest,
                    id = id
                )

                call.respond(
                    status = response.statusCode,
                    message = response
                )
            }
        }
    }

    post("reset-password/request") {
        val requestBody = call.receive<ResetPasswordRequestRequest>()

        val response = userService
            .resetPasswordRequest(requestBody.email)

        call.respond(
            status = HttpStatusCode.OK,
            message = response
        )

    }

    post("reset-password/validate") {
        val requestBody = call.receive<ValidateOtpRequest>()

        val response = userService.validateResetOtp(
            userNameOrEmail = requestBody.email,
            otp = requestBody.otp
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = response
        )

    }

    put("reset-password") {
        val requestBody = call.receive<ResetPasswordRequest>()

        val response = userService.resetPassword(
            token = requestBody.token,
            newPassword = requestBody.newPassword
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = response.message
        )
    }
}