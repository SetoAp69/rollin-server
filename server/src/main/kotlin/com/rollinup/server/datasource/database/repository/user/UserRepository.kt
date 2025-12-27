package com.rollinup.server.datasource.database.repository.user

import com.rollinup.server.datasource.database.model.user.UserEntity
import com.rollinup.server.model.request.user.EditUserRequest
import com.rollinup.server.model.request.user.RegisterUserRequest
import com.rollinup.server.model.request.user.UserQueryParams
import com.rollinup.server.model.response.user.GetUserOptionsResponse

interface UserRepository {

    fun getAllUsers(queryParams: UserQueryParams): List<UserEntity>

    fun createUser(request: RegisterUserRequest)

    fun editUser(request: EditUserRequest, id: String)

    fun deleteUser(id: List<String>)

    fun getUserById(id: String): UserEntity?

    fun checkDevice(deviceId:String): Boolean

    fun getUserByEmailOrUsername(emailOrUsername: String): UserEntity?

    fun checkEmailOrUsername(email:String?, username:String?):Boolean

    fun resetPassword(id: String, newPassword: String, salt: String)

    fun updatePasswordAndDevice(id:String, salt:String, password:String, deviceId:String?)

    fun getUserOptions(): GetUserOptionsResponse


}