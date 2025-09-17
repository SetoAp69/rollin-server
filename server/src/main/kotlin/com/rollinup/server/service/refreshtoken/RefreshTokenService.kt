package com.rollinup.server.service.refreshtoken

interface RefreshTokenService {
    fun refreshToken(token:String):String

    fun generateToken(id:String):String
}