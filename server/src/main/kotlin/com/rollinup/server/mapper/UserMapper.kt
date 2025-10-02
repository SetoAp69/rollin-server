package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.user.GetAllUserResponse
import com.rollinup.server.model.response.user.ResetPasswordRequestResponse
import com.rollinup.server.model.response.user.UserDTO
import com.rollinup.server.model.response.user.ValidateResetOtpResponse

class UserMapper {
    fun mapGetAllUserResponse(
        data: List<UserEntity>,
        queryParams: UserQueryParams
    ): GetAllUserResponse {
        return GetAllUserResponse(
            record = data.size,
            page = queryParams.page ?: 1,
            message = "",
            data = data.map {
                UserDTO(
                    id = it.id,
                    userName = it.userName,
                    email = it.email,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    role = it.role.value,
                    gender = it.gender
                )
            },
        )
    }

    fun mapValidateResetOtpResponse(
        resetToken: String
    ): ValidateResetOtpResponse {
        return ValidateResetOtpResponse(
            resetToken = resetToken
        )
    }

    fun mapResetPasswordRequestResponse(
        email: String
    ): ResetPasswordRequestResponse {
        return ResetPasswordRequestResponse(
            email = email
        )
    }

}