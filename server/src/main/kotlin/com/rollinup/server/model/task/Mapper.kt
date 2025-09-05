package com.rollinup.server.model.task

import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    newSuspendedTransaction(
        context = Dispatchers.IO, statement = block

    )

fun daoToModel(dao: TaskDAO) = Task(
    name = dao.name,
    descriptions = dao.description,
    priority = Priority.fromValue(dao.priority)
)