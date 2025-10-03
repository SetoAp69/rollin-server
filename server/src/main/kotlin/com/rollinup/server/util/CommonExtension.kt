package com.rollinup.server.util

import JwtAuthClaim
import com.rollinup.server.CommonException
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.routing.RoutingCall
import io.ktor.server.routing.RoutingContext
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

fun RoutingCall.getAuthClaim(): JwtAuthClaim {
    fun claim(name: String): String =
        this.principal<JWTPrincipal>()
            ?.payload
            ?.getClaim(name)
            ?.asString()
            ?: throw IllegalStateException("Invalid token")

    return JwtAuthClaim(
        id = claim("id"),
        username = claim("username"),
        email = claim("email"),
        _role = claim("role")
    )
}

suspend fun RoutingContext.withClaim(
    body: suspend RoutingContext.(JwtAuthClaim) -> Unit
) {
    val claim = call.getAuthClaim()
    body(claim)
}


fun String.notFoundException(): CommonException {
    return CommonException("can't found $this data")
}

fun String.isExistException(): CommonException {
    return CommonException("$this data is already exist")
}

fun String.successGettingResponse(): String {
    return "Success getting $this data"
}

fun String.successCreateResponse(): String {
    return "$this data successfully created"
}

fun String.successEditResponse(): String {
    return "$this data successfully updated"
}

fun String.toCensoredEmail(): String {
    val email = this.substringBefore("@")
    return "${email.firstOrNull() ?: "*"}*****${email.lastOrNull() ?: "*"}@***.***"
}