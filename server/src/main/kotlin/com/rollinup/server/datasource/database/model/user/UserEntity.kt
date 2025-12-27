package com.rollinup.server.datasource.database.model.user

import com.rollinup.server.model.Role

data class UserEntity(
    val id: String = "",
    val userName: String = "",
    val email: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val studentId: String? = null,
    val role: Role = Role(),
    val gender: String = "",
    val password: String = "",
    val salt: String = "",
    val device: String? = null,
    val phoneNumber: String? = null,
    val classX: Class? = null,
    val address:String ="",
    val birthday:String ="",
    val isVerified:Boolean = false
) {
    data class Role(
        val id:String = "",
        val key:Int = 0,
        val name:String = ""
    )
    data class Class(
        val id: String = "",
        val key: Int = 0,
        val name: String = "",
        val grade: Int = 0,
    )
}
