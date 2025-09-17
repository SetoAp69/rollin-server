package com.rollinup.server.datasource.database.model.task

import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction


fun daoToModel(dao: TaskDAO) = Task(
    name = dao.name,
    descriptions = dao.description,
    priority = Priority.fromValue(dao.priority)
)