package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.user.GetAllUserResponse
import com.rollinup.server.model.response.user.GetUserByIdResponse
import com.rollinup.server.model.response.user.ResetPasswordRequestResponse
import com.rollinup.server.model.response.user.UserDTO
import com.rollinup.server.model.response.user.ValidateResetOtpResponse
import com.rollinup.server.model.response.user.ValidateVerificationOtpResponse

class UserMapper {
    fun mapGetAllUserResponse(
        data: List<UserEntity>,
        queryParams: UserQueryParams,
    ): GetAllUserResponse {
        return GetAllUserResponse(
            record = data.size,
            page = queryParams.page ?: 1,
            data = data.map {
                UserDTO(
                    id = it.id,
                    userName = it.userName,
                    email = it.email,
                    firstName = it.firstName,
                    lastName = it.lastName,
                    role = it.role.name,
                    gender = it.gender,
                    classX = it.classX?.name,
                    classId = it.classX?.id,
                    classKey = it.classX?.key,
                    isVerified = it.isVerified,
                    address = it.address,
                    studentId = it.studentId
                )
            },
        )
    }

    fun mapGetUserByIdResponse(
        data: UserEntity,
    ) = GetUserByIdResponse(
        id = data.id,
        username = data.userName,
        firsName = data.firstName,
        lastName = data.lastName,
        studentId = data.studentId,
        phoneNumber = data.phoneNumber,
        role = data.role.let {
            GetUserByIdResponse.Role(
                id = it.id,
                name = it.name,
                key = it.key
            )
        },
        classX = data.classX?.let {
            GetUserByIdResponse.Class(
                id = it.id,
                key = it.key,
                name = it.name,
                grade = it.grade
            )
        },
        address = data.address,
        gender = data.gender,
        email = data.email,
        birthday = data.birthday
    )

    fun mapValidateResetOtpResponse(
        resetToken: String,
    ): ValidateResetOtpResponse {
        return ValidateResetOtpResponse(
            resetToken = resetToken
        )
    }

    fun mapResetPasswordRequestResponse(
        email: String,
    ): ResetPasswordRequestResponse {
        return ResetPasswordRequestResponse(
            email = email
        )
    }

    fun mapValidateVerificationOtpResponse(
        token: String,
    ): ValidateVerificationOtpResponse {
        return ValidateVerificationOtpResponse(
            token = token
        )
    }
}