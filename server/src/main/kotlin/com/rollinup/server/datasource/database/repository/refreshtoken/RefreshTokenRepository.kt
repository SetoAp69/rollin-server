package com.rollinup.server.datasource.database.repository.refreshtoken

import com.rollinup.server.datasource.database.model.user.UserEntity

interface RefreshTokenRepository {
    fun save(token: String, id: String)

    fun dropToken(token: String)

    fun findUserId(token: String): UserEntity?
}