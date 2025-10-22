package com.rollinup.server.datasource.database.table

import com.rollinup.server.datasource.database.model.user.Gender
import com.rollinup.server.datasource.database.model.user.PGEnum
import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object UserTable : Table("users") {
    val user_id = uuid("user_id")
    val username = varchar("username", 50)
    val studentId = varchar("student_id",30).nullable()
    val email = varchar("email", 100)
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val address = varchar("address", 100)
    val password = varchar("password", 128)
    val salt = varchar("salt", 64)
    val phoneNumber = varchar("phone_number", 15)
    val role = reference(
        name = "role",
        refColumn = RoleTable._id,
        fkName = "fkey_role",
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE,
    )
    val gender: Column<Gender> = customEnumeration(
        name = "gender",
        fromDb = { gender -> Gender.fromValue(gender as String) },
        toDb = { gender -> PGEnum("gender", gender) }
    )

    val device = varchar("device", 30).nullable()
    val profilePicture = varchar("profile_pictures", 120).nullable()
    val classX = reference(
        name = "class",
        refColumn = ClassTable._id,
        onDelete = ReferenceOption.CASCADE ,
        onUpdate = ReferenceOption.CASCADE,
        fkName = "fk_user_class"
    )

    val searchField
        get() = listOf(
            username,
            email,
            firstName,
            lastName,
            address,
            phoneNumber
        )

    val sortField
        get() = mapOf(
            "username" to username,
            "email" to email,
            "first_name" to firstName,
            "last_name" to lastName,
            "gender" to gender
        )

    val filterField
        get() = listOf(
            gender,
        )
}
/*
* 21 x 30 = 630
* 18 x 30 = 540
*
* */