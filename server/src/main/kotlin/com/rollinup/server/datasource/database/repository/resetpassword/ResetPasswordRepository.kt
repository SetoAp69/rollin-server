package com.rollinup.server.datasource.database.repository.resetpassword

import com.rollinup.server.datasource.database.model.resetpassword.ResetPasswordEntity

interface ResetPasswordRepository {
    fun getToken(id: String): ResetPasswordEntity?

    fun saveToken(id: String, token: String, salt: String)

    fun deleteToken(id: String)
}