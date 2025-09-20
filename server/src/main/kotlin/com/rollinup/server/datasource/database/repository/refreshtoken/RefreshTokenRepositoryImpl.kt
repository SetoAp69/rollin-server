package com.rollinup.server.datasource.database.repository.refreshtoken

import com.rollinup.server.datasource.database.dao.refreshtoken.RefreshTokenDao
import com.rollinup.server.datasource.database.dao.refreshtoken.RefreshTokenDaoImpl
import com.rollinup.server.datasource.database.model.user.UserDTO
import com.rollinup.server.datasource.database.table.RefreshTokenTable
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.UUID


class RefreshTokenRepositoryImpl(
    private val refreshTokenDao: RefreshTokenDao
) : RefreshTokenRepository {

    override fun save(token: String, id: String) {
        return refreshTokenDao.saveToken(
            token = token,
            id = id
        )
    }

    override fun dropToken(token: String) {
        return refreshTokenDao.dropToken(token)
    }

    override fun findUserId(token: String): UserDTO? {
        return refreshTokenDao.findUserByToken(
            token = token,
        )
    }
}