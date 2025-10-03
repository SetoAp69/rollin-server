package com.rollinup.server.plugin

import com.rollinup.server.IllegalRoleException
import com.rollinup.server.model.Role
import com.rollinup.server.util.Message
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.createRouteScopedPlugin
import io.ktor.server.auth.AuthenticationChecked
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal

class RbacConfig {
    var roles: Set<Role> = emptySet()
}

val RbacPlugin = createRouteScopedPlugin(
    name = "Rbac",
    createConfiguration = ::RbacConfig
) {
    val roles = pluginConfig.roles

    pluginConfig.apply {
        on(AuthenticationChecked) { call ->
            val tokenRole = getRoleFromToken(call)
                ?: throw IllegalStateException(Message.INVALID_TOKEN)

            val authorized = roles.contains(tokenRole)
            if (!authorized) {
                throw IllegalRoleException()
            }
        }
    }
}

private fun getRoleFromToken(call: ApplicationCall): Role? =
    Role.fromValue(
        call.principal<JWTPrincipal>()
            ?.payload
            ?.getClaim("role")
            ?.asString()
    )
