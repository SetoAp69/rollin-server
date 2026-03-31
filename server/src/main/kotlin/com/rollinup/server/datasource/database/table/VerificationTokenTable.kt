package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestampWithTimeZone

object VerificationTokenTable : Table("verification_token") {
    val user_id = uuid("user_id").references(UserTable.user_id)
    val token = varchar("token", 264)
    val salt = varchar("salt", 264)
    val expiredAt = timestampWithTimeZone("expired_at")
}