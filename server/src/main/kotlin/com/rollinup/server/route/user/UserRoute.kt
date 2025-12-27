package com.rollinup.server.route.user

import com.rollinup.server.configurations.withRole
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.ListIdBody
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.ResetPasswordRequest
import com.rollinup.server.model.request.user.ResetPasswordRequestRequest
import com.rollinup.server.model.request.user.UpdatePasswordAndDeviceRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.request.user.ValidateResetPasswordOtpRequest
import com.rollinup.server.model.request.user.ValidateVerificationOtpRequest
import com.rollinup.server.service.user.UserService
import com.rollinup.server.util.Utils.stringJsonToList
import com.rollinup.server.util.missingArgumentException
import com.rollinup.server.util.withClaim
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import org.koin.ktor.ext.inject

fun Route.userRouteNew() {
    val userService by inject<UserService>()

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            post {
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
        withRole(Role.ADMIN, Role.TEACHER, Role.STUDENT) {
            get("/check-email-username") {
                val email = call.queryParameters["email"]
                val username = call.queryParameters["username"]
                val response = userService.checkEmailUserName(email, username)
                call.respond(response.statusCode, response)

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
                        gender = call.queryParameters["gender"]?.stringJsonToList(),
                        classX = call.queryParameters["class"]?.stringJsonToList(),
                        role = call.queryParameters["role"]?.stringJsonToList()
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
        withRole(Role.ADMIN, Role.STUDENT, Role.TEACHER) {
            patch("/{id}") {
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

    post("/reset-password/request") {
        val requestBody = call.receive<ResetPasswordRequestRequest>()

        val response = userService
            .resetPasswordRequest(requestBody.email)

        call.respond(
            status = HttpStatusCode.OK,
            message = response
        )
    }

    post("/reset-password/validate") {
        val requestBody = call.receive<ValidateResetPasswordOtpRequest>()

        val response = userService.validateResetOtp(
            userNameOrEmail = requestBody.email,
            otp = requestBody.otp
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = response
        )

    }

    put("/reset-password") {
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

    authenticate("auth-jwt") {
        post("/update-password-and-verification/validate") {
            withClaim { claim ->
                val id = claim.id
                val requestBody = call.receive<ValidateVerificationOtpRequest>()
                val response = userService.validateVerificationOtp(id, requestBody.otp)

                call.respond(
                    status = response.statusCode,
                    message = response
                )
            }
        }
    }

    authenticate("auth-jwt") {
        get("/resend-verification-otp") {
            withClaim { claim ->
                val id = claim.id
                val response = userService.resendVerificationOtp(id)
                call.respond(
                    status = response.statusCode,
                    message = response
                )

            }
        }
    }

    authenticate("auth-jwt") {
        patch("/update-password-and-verification") {
           withClaim { claim->
               val body = call.receive<UpdatePasswordAndDeviceRequest>()
               val response = userService.updatePasswordAndVerify(body)
               call.respond(status = HttpStatusCode.OK, message = response.message)
           }
        }
    }

    authenticate("auth-jwt") {
        get("/options") {
            val response = userService.getUserOptions()

            call.respond(status = response.statusCode, message = response)
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            delete {
                val body = call.receive<ListIdBody>()
                val response = userService.deleteUsers(body)
                call.respond(status = response.statusCode, message = response.message)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.TEACHER, Role.STUDENT, Role.ADMIN) {
            get("/{id}") {
                val id = call.pathParameters["id"]
                    ?: throw "id".missingArgumentException()

                val response = userService.getUserById(id)
                call.respond(status = response.statusCode, message = response)
            }
        }
    }
}