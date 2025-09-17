package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.datasource.database.dao.auth.UserDao
import com.rollinup.server.model.response.auth.User
import com.rollinup.server.model.auth.UserCreateEditRequest
import com.rollinup.server.model.auth.UserQueryParams

class UserRepositoryImpl(
    private val dao: UserDao
) : UserRepository {
    override suspend fun basicAuth(
        userName: String
    ): User? {
        return dao.basicAuth(
            userName = userName,
        )
    }

    override suspend fun getAllUsers(queryParams: UserQueryParams): List<User> {
        return dao.getAllUsers(queryParams)
    }

    override suspend fun getUserById(id: String): User? {
        return null
    }

    override suspend fun registerUser(createRequest: UserCreateEditRequest) {
        return dao.createUser(createRequest)
    }

    override suspend fun editUser(editRequest: UserCreateEditRequest) {
        return dao.editUser(editRequest)
    }
}