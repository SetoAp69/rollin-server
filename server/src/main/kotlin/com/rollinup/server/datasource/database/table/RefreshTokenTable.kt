package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table

object RefreshTokenTable : Table("refresh_token") {
    val user_id = uuid("user_id")
    val token = varchar("token", 256)
}