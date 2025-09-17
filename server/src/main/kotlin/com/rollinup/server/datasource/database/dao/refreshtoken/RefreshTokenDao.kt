package com.rollinup.server.datasource.database.dao.refreshtoken

import com.rollinup.server.datasource.database.model.task.TaskDAO
import com.rollinup.server.datasource.database.table.RefreshTokenTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class RefreshTokenDao(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<RefreshTokenDao>(RefreshTokenTable)

    var user_id by RefreshTokenTable.user_id
    var token by RefreshTokenTable.token
}