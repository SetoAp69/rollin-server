package com.rollinup.server.configurations

import com.rollinup.server.util.Config
import io.ktor.server.application.Application
import org.jetbrains.exposed.v1.core.DatabaseConfig
import org.jetbrains.exposed.v1.core.SqlLogger
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.core.statements.StatementContext
import org.jetbrains.exposed.v1.jdbc.Database

fun Application.configureDatabase() {

    val config = Config.getDbConfig()

    Database.connect(
        url = config.url,
        user = config.username,
        password = config.password,

        databaseConfig = DatabaseConfig(
            body = {
                sqlLogger = object : SqlLogger {
                    override fun log(
                        context: StatementContext,
                        transaction: Transaction
                    ) {
                        println("transaction : $transaction")
                    }

                }
            }
        ),

        )
}
