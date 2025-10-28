package com.rollinup.server.route.generalsetting

import com.rollinup.server.configurations.withRole
import com.rollinup.server.generalsetting.GeneralSettingEventBus
import com.rollinup.server.mapper.GeneralSettingMapper
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import com.rollinup.server.service.generalsetting.GeneralSettingService
import com.rollinup.server.util.withClaim
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.send
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.milliseconds

fun Route.generalSettingRoute() {
    val service by inject<GeneralSettingService>()
    val eventBus by inject<GeneralSettingEventBus>()
    val mapper by inject<GeneralSettingMapper>()

    authenticate("auth-jwt") {
        withRole(Role.ADMIN, Role.STUDENT, Role.TEACHER) {
            get {
                val response = service.getGeneralSetting()
                call.respond(status = response.statusCode, message = response)
            }
        }
    }

    authenticate("auth-jwt") {
        withRole(Role.ADMIN) {
            put("/edit") {
                withClaim { claim ->
                    val body = call.receive<EditGeneralSettingBody>()
                    val response = service.updateGeneralSetting(body = body, editBy = claim.id)
                    call.respond(status = response.statusCode, message = response.message)
                }
            }
        }
    }

    sse("/sse", serialize = { typeInfo, it ->
        val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
        Json.encodeToString(serializer, it)
    }) {
        heartbeat {
            period = 10.milliseconds
            event = ServerSentEvent("heartbeat")
        }
        eventBus.events.collectLatest {
            val data = mapper.mapGetGeneralSettingResponse(it)
            send(data)
        }
    }
}