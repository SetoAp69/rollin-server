package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp

object ResetPasswordTokenTable : Table("reset_password_token") {
    val userId = uuid("user_id").references(UserTable.user_id)
    val token = varchar(name = "token", length = 256)
    val salt = varchar(name = "salt", length = 256)
    val expiredAt = timestamp(name = "expired_at")
}