package com.rollinup.server.service.auth

import com.rollinup.server.model.request.auth.LoginRequest
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.auth.LoginResponse
import com.rollinup.server.model.response.auth.RefreshTokenResponse

interface AuthService {
    suspend fun login(loginRequest: LoginRequest): Response<LoginResponse>

    suspend fun refreshToken(token:String):Response<RefreshTokenResponse>
}