package com.rollinup.server.datasource.database.table

import com.rollinup.server.datasource.database.model.user.Gender
import com.rollinup.server.datasource.database.model.user.PGEnum
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable

object UserTable : IntIdTable("users") {
    val _id = uuid("id")
    val username = varchar("username", 50)
    val email = varchar("email", 100)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val address = varchar("address", 100)
    val password = varchar("password", 50)
    val salt = varchar("salt",64)
    val role = reference("role", RoleTable._id)
    val gender: Column<Gender> = customEnumeration(
        name = "gender",
        fromDb = { gender -> Gender.fromValue(gender as String) },
        toDb = { gender -> PGEnum("gender", gender) }
    )
}