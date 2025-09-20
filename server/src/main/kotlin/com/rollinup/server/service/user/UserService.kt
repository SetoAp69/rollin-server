package com.rollinup.server.service.user

import com.rollinup.server.model.request.user.RegisterEditUserRequest

interface UserService {
    fun registerUser(registerEditUserRequest: RegisterEditUserRequest)

    fun editUser(registerEditUserRequest: RegisterEditUserRequest, id:String)

    fun resetPassword(token:String, newPassword:String)

    fun resetPasswordRequest(usernameOrEmail:String):String
}