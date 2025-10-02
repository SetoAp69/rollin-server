package com.rollinup.server.mapper

import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.model.response.auth.LoginResponse
import com.rollinup.server.model.response.user.UserDTO

class AuthMapper {
    fun mapLoginResponse(
        data: UserEntity,
        accessToken: String,
        refreshToken: String
    ): LoginResponse =
        LoginResponse(
            data = UserDTO(
                id = data.id,
                userName = data.userName,
                email = data.email,
                firstName = data.firstName,
                lastName = data.lastName,
                role = data.role.value,
                gender = data.gender
            ),
            accessToken = accessToken,
            refreshToken = refreshToken
        )
}