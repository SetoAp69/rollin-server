package com.rollinup.server.datasource.database.repository.refreshtoken

interface RefreshTokenRepository {
    fun save(token:String, id:String)

    fun dropToken(token:String)

    fun findUserId(token:String):String?
}