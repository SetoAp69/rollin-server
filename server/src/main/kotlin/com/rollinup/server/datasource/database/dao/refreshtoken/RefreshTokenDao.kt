package com.rollinup.server.datasource.database.dao.refreshtoken

import com.rollinup.server.datasource.database.model.user.UserDTO

interface RefreshTokenDao {
    fun saveToken(token:String, id:String)

    fun dropToken(token:String)

    fun findUserByToken(token:String): UserDTO?

}