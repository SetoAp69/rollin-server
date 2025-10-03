package com.rollinup.server.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
    withContext(context = Dispatchers.IO) {
        transaction {
            block()
        }
    }
