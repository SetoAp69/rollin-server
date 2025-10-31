package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table

object ClassTable: Table("class") {
    val _id = uuid("_id")
    val name = varchar("name", 30)
    val grade = integer("grade")
    val key = integer("key")

}