package com.rollinup.server.util.manager

import org.jetbrains.exposed.v1.core.Transaction

interface TransactionManager {
    suspend fun <T> suspendTransaction(block: Transaction.() -> T): T
}