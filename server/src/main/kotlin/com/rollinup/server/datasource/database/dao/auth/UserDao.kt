package com.rollinup.server.datasource.database.dao.auth

import com.rollinup.server.model.auth.UserQueryParams
import com.rollinup.server.model.request.user.RegisterEditUserRequest
import com.rollinup.server.model.response.auth.User

interface UserDao {

    fun basicAuth(userName: String): User?

    fun getAllUsers(queryParams: UserQueryParams): List<User>

    fun createUser(request: RegisterEditUserRequest)

    fun editUser(request: RegisterEditUserRequest, id: String)

    fun deleteUser(id: String)

    fun getUserById(id: String): User?

    fun getUserByEmailOrUsername(emailOrUsername: String): User?

    fun resetPassword(emailOrUsername: String, newPassword: String)


}