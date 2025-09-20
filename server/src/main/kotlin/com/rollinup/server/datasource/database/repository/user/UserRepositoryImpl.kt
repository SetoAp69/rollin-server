package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.datasource.database.dao.auth.UserDao
import com.rollinup.server.model.auth.UserQueryParams
import com.rollinup.server.model.request.user.RegisterEditUserRequest
import com.rollinup.server.model.response.auth.User

class UserRepositoryImpl(
    private val dao: UserDao
) : UserRepository {
    override  fun basicAuth(
        userName: String
    ): User? {
        return dao.basicAuth(
            userName = userName,
        )
    }

    override  fun getAllUsers(queryParams: UserQueryParams): List<User> {
        return dao.getAllUsers(queryParams)
    }

    override  fun getUserById(id: String): User? {
        return dao.getUserById(id)
    }

    override  fun getUserByEmailOrUsername(emailOrUsername: String): User? {
        return dao.getUserByEmailOrUsername(emailOrUsername)
    }

    override fun resetPassword(
        emailOrUsername: String,
        newPassword: String
    ) {
        return dao.resetPassword(
            emailOrUsername = emailOrUsername,
            newPassword = newPassword
        )
    }

    override  fun registerUser(createRequest: RegisterEditUserRequest) {
        return dao.createUser(createRequest)
    }

    override  fun editUser(editRequest: RegisterEditUserRequest, id: String) {
        return dao.editUser(editRequest, id)
    }
}