package com.rollinup.server.service.user

import com.rollinup.server.model.request.ListIdBody
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UpdatePasswordAndDeviceRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.user.GetAllUserResponse
import com.rollinup.server.model.response.user.GetUserByIdResponse
import com.rollinup.server.model.response.user.GetUserOptionsResponse
import com.rollinup.server.model.response.user.ResendOtpResponse
import com.rollinup.server.model.response.user.ResetPasswordRequestResponse
import com.rollinup.server.model.response.user.ValidateResetOtpResponse
import com.rollinup.server.model.response.user.ValidateVerificationOtpResponse

interface UserService {
    suspend fun registerUser(requestBody: RegisterUserRequest): Response<Unit>

    suspend fun editUser(
        requestBody: EditUserRequest,
        id: String,
    ): Response<Unit>

    suspend fun getAllUser(queryParams: UserQueryParams): Response<GetAllUserResponse>

    suspend fun getUserById(id: String): Response<GetUserByIdResponse>

    suspend fun validateResetOtp(
        userNameOrEmail: String,
        otp: String,
    ): Response<ValidateResetOtpResponse>

    suspend fun resetPasswordRequest(usernameOrEmail: String): Response<ResetPasswordRequestResponse>

    suspend fun resetPassword(token: String, newPassword: String): Response<Unit>

    suspend fun getUserOptions(): Response<GetUserOptionsResponse>

    suspend fun deleteUsers(body: ListIdBody): Response<Unit>

    suspend fun checkEmailUserName(email: String?, username: String?): Response<Unit>

    suspend fun validateVerificationOtp(
        id: String,
        otp: String,
    ): Response<ValidateVerificationOtpResponse>

    suspend fun resendVerificationOtp(id: String): Response<ResendOtpResponse>

    suspend fun updatePasswordAndVerify(
        body: UpdatePasswordAndDeviceRequest,
    ): Response<Unit>
}