package com.rollinup.server.configurations

import com.rollinup.server.model.Role
import com.rollinup.server.plugin.RbacPlugin
import io.ktor.server.routing.Route


fun Route.withRole(
    vararg role: Role,
    build: Route.() -> Unit
) {
    install(RbacPlugin) {
        roles = role.toSet()
    }
    build()
}
