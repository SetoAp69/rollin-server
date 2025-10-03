//package com.rollinup.server
//
//import org.jetbrains.exposed.v1.core.Transaction
//import org.jetbrains.exposed.v1.core.transactions.TransactionManagerApi
//import org.jetbrains.exposed.v1.jdbc.transactions.TransactionManager
//
//class TransactionManagerTest: TransactionManagerApi{
//    override var defaultReadOnly: Boolean
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var defaultMaxAttempts: Int
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var defaultMinRetryDelay: Long
//        get() = TODO("Not yet implemented")
//        set(value) {}
//    override var defaultMaxRetryDelay: Long
//        get() = TODO("Not yet implemented")
//        set(value) {}
//
//    override fun currentOrNull(): Transaction? {
//        TODO("Not yet implemented")
//    }
//
//    override fun bindTransactionToThread(transaction: Transaction?) {
//        TODO("Not yet implemented")
//    }
//
//}