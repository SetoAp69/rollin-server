package com.rollinup.server.di

import com.rollinup.server.datasource.database.dao.auth.UserDao
import com.rollinup.server.datasource.database.dao.auth.UserDaoImpl
import com.rollinup.server.datasource.database.dao.refreshtoken.RefreshTokenDao
import com.rollinup.server.datasource.database.dao.refreshtoken.RefreshTokenDaoImpl
import org.koin.dsl.module

object DAOModule {
    val module = module {
        single<UserDao> {
            UserDaoImpl()
        }
        single<RefreshTokenDao> {
            RefreshTokenDaoImpl()
        }
    }
}