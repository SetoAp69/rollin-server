package com.rollinup.server.service.refreshtoken

import com.rollinup.server.service.jwt.TokenService

interface RefreshTokenService{
    fun refreshToken(token:String):String

    fun generateToken(id:String):String
}