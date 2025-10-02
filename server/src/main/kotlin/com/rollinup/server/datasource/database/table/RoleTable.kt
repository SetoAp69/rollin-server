package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table

object RoleTable : Table("role") {
    val _id = uuid("role_id")
    val name = varchar("name", 50)
    val searchField
        get() = listOf(name)
    val sortField
        get() = mapOf("role" to name)


}