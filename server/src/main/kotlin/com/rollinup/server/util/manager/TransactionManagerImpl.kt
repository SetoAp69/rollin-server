package com.rollinup.server.util.manager

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class TransactionManagerImpl : TransactionManager {
    override suspend fun <T> suspendTransaction(block: Transaction.() -> T): T {
        return withContext(context = Dispatchers.IO) {
            transaction {
                block()
            }
        }
    }
}