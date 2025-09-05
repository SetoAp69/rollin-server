package com.rollinup.server.configurations

import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.koin.ktor.ext.inject

fun Application.configureDatabase(){
    val config by inject<Configuration>()

    val dbUrl = config.fetchProperty("DATABASE_URL")
    val dbUsername = config.fetchProperty("DATABASE_USERNAME")
    val dbPassword = config.fetchProperty("DATABASE_PASSWORD")

    Database.connect(
        url = dbUrl,
        user =  dbUsername,
        password = dbPassword,
//        setupConnection ={it->
//
//        } ,
        databaseConfig = DatabaseConfig(
            body = {
                sqlLogger = object: SqlLogger {
                    override fun log(
                        context: StatementContext,
                        transaction: Transaction
                    ) {
                        println("transaction : $transaction")
                    }

                }
            }
        ) ,
//        connectionAutoRegistration = ,
//        manager = ,
    )
}