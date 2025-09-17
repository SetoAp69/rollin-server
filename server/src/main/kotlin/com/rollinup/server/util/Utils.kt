package com.rollinup.server.util

import com.rollinup.server.model.Task
import com.rollinup.server.service.jwt.TokenConfig
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.core.Transaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object Utils {
    fun Task.taskAsRow(): String {
        return """
            <tr>
                <td>$name</td>
                <td>$descriptions</td>
                <td>$priority</td>
            </tr>
        """.trimIndent()
    }

    fun List<Task>.taskAsTable(): String {
        return this.joinToString(
            prefix = "<table rules=\"all\">",
            postfix = "</table>",
            separator = "\n",
            transform = { task -> task.taskAsRow() }
        )
    }

    fun getTokenConfig(): TokenConfig{
        return TokenConfig(
            issuer = "",
            audience = "",
            expiresIn = 0L,
            secret = ""
        )
    }

    suspend fun <T> suspendTransaction(block: Transaction.() -> T): T =
        withContext(context = Dispatchers.IO) {
            transaction {
                block()
            }
        }

    suspend inline fun <reified T> HttpResponse.handleBody(): T {
        if (this.status.value in 200..299) {
            return body<T>()
        } else {
            throw Exception(this.status.description)
        }
    }
}