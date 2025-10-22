package com.rollinup.server.configurations

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.IllegalRoleException
import com.rollinup.server.UnauthorizedTokenException
import com.rollinup.server.model.response.Response
import com.rollinup.server.util.Message
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureStatusPage() {
    install(StatusPages) {
        exception<Throwable> { call, error ->
            when (error) {
                is RequestValidationException -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = error.reasons
                    )
                }

                is UnauthorizedTokenException -> {
                    call.respond(
                        status = HttpStatusCode.Unauthorized,
                        message = Message.UNAUTHORIZED_TOKEN
                    )
                }

                is IllegalPathParameterException -> {
                    call.respond(
                        status = HttpStatusCode.BadRequest,
                        message = "Illegal path parameter : ${error.message}"
                    )
                }

                is IllegalRoleException -> {
                    call.respond(
                        status = HttpStatusCode.Forbidden,
                        message = Message.ILLEGAL_ROLE
                    )
                }

                else -> {
                    call.respond(
                        status = HttpStatusCode.InternalServerError,
                        message = Response(
                            status = 500,
                            message = error.message.toString(),
                            data = Unit
                        )
                    )
                }
            }
        }

    }

}