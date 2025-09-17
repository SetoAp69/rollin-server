package com.rollinup.server.model.register

data class AdminAuth(
    val accessToken:String = "",
    val expiresIn:Int = 0,
)
