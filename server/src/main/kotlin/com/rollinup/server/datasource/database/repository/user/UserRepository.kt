package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.model.response.auth.User
import com.rollinup.server.model.auth.UserCreateEditRequest
import com.rollinup.server.model.auth.UserQueryParams

interface UserRepository {
    suspend fun basicAuth(userName: String): User?

    suspend fun getAllUsers(queryParams: UserQueryParams): List<User>

    suspend fun getUserById(id: String): User?

    suspend fun registerUser(createRequest: UserCreateEditRequest)

    suspend fun editUser(editRequest: UserCreateEditRequest)

}