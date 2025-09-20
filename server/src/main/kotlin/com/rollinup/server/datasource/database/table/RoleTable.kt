package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RoleTable : Table("role") {
    val _id = uuid("role_id")
    val name = varchar("name", 50)
}