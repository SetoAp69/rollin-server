package com.rollinup.server.service.user

import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterDeviceBody
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.user.GetAllUserResponse
import com.rollinup.server.model.response.user.ResetPasswordRequestResponse
import com.rollinup.server.model.response.user.ValidateResetOtpResponse

interface UserService {
    suspend fun registerUser(requestBody: RegisterUserRequest): Response<Unit>

    suspend fun editUser(
        requestBody: EditUserRequest,
        id: String
    ): Response<Unit>

    suspend fun getAllUser(queryParams: UserQueryParams): Response<GetAllUserResponse>

    suspend fun validateResetOtp(
        userNameOrEmail: String,
        otp: String
    ): Response<ValidateResetOtpResponse>


    suspend fun resetPasswordRequest(usernameOrEmail: String): Response<ResetPasswordRequestResponse>

    suspend fun resetPassword(token: String, newPassword: String): Response<Unit>

    suspend fun registerDevice(id:String, body: RegisterDeviceBody): Response<Unit>

}


/*
* New Acc -> Login -> 201 (Device = null) -> Register Device Page -> register(deviceId) ->
* Verified Acc -> Login -> 200 -> Local check -> devId is Invalid -> logout
*
* */