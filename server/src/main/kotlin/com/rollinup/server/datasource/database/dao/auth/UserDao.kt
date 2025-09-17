package com.rollinup.server.datasource.database.dao.auth

import com.rollinup.server.model.response.auth.User
import com.rollinup.server.model.auth.UserCreateEditRequest
import com.rollinup.server.model.auth.UserQueryParams

interface UserDao {

    fun basicAuth(userName: String): User?

    fun getAllUsers(queryParams: UserQueryParams): List<User>

    fun createUser(request: UserCreateEditRequest)

    fun editUser(request: UserCreateEditRequest)

    fun deleteUser(id: String)

    fun getUserById(id: String): User?


}