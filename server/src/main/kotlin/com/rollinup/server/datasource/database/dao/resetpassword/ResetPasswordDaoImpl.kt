package com.rollinup.server.datasource.database.dao.resetpassword

import com.rollinup.server.datasource.database.table.ResetPasswordTokenTable
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.jdbc.Query
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.upsert
import java.util.UUID

//class ResetPasswordDaoImpl : ResetPasswordDao {
//
//}