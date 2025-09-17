package com.rollinup.server.service.auth

import com.rollinup.server.datasource.database.model.user.UserDTO
import com.rollinup.server.model.auth.LoginRequest
import com.rollinup.server.model.auth.UserQueryParams

interface AuthService {
    suspend fun basicAuth(authRequest: LoginRequest): UserDTO

    suspend fun getAllUsers(queryParams: UserQueryParams): List<UserDTO>

    suspend fun getUserById(id: String): UserDTO
}