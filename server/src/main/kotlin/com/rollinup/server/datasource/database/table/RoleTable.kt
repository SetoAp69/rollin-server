package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object RoleTable : IntIdTable("roles") {
    val _id = uuid("id")
    val name = varchar("name", 50)
}