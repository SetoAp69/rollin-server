package com.rollinup.server.configurations

import com.rollinup.server.model.request.ListIdBody
import com.rollinup.server.model.request.attendance.CreateAttendanceRequest
import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.request.permit.PermitApprovalBody
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RefreshTokenRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.ResetPasswordRequest
import com.rollinup.server.model.request.user.UpdatePasswordAndDeviceRequest
import com.rollinup.server.model.request.user.ValidateVerificationOtpRequest
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidation

fun Application.configureValidator() {
    install(RequestValidation) {
        validate<LoginRequest> {
            it.validation()
        }
        validate<RefreshTokenRequest> {
            it.validation()
        }
        validate<RegisterUserRequest> {
            it.validation()
        }
        validate<UpdatePasswordAndDeviceRequest> {
            it.validation()
        }
        validate<ResetPasswordRequest> {
            it.validation()
        }
        validate<ValidateVerificationOtpRequest> {
            it.validation()
        }
        validate<EditUserRequest> {
            it.validation()
        }
        validate<ListIdBody> {
            it.validation()
        }
        validate<PermitApprovalBody> {
            it.validation()
        }
        validate<CreateAttendanceRequest> {
            it.validation()
        }
    }
}