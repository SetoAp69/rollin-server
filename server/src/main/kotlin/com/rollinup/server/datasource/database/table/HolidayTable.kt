package com.rollinup.server.datasource.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.date
import java.util.UUID

object HolidayTable : Table("holiday") {
    var _id = uuid("_id").clientDefault { UUID.randomUUID() }
    var name = varchar("name", 30)
    var date = date("date")
}