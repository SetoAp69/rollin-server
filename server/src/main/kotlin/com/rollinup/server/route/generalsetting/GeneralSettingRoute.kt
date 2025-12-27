package com.rollinup.server.route.generalsetting

import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.generalsetting.GeneralSettingEventBus
import com.rollinup.server.configurations.withRole
import com.rollinup.server.mapper.GeneralSettingMapper
import com.rollinup.server.model.MapConfig
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import com.rollinup.server.service.generalsetting.GeneralSettingService
import com.rollinup.server.util.withClaim
import io.ktor.server.auth.authenticate
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.put
import io.ktor.server.sse.heartbeat
import io.ktor.server.sse.send
import io.ktor.server.sse.sse
import io.ktor.server.thymeleaf.ThymeleafContent
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import org.koin.ktor.ext.inject
import kotlin.time.Duration.Companion.milliseconds

fun Route.generalSettingRoute() {
    val service by inject<GeneralSettingService>()
    val eventBus by inject<GeneralSettingEventBus>()
    val mapper by inject<GeneralSettingMapper>()
    val settingCache by inject<GeneralSettingCache>()

    staticResources("/static", "map/static")

    get("/map") {
        val setting = settingCache.current ?: return@get
        val mapApiKey = System.getenv("GOOGLE_MAPS_API_KEY") ?: return@get
        val mapConfig = MapConfig(
            initialLat = setting.lat,
            initialLong = setting.long,
            initialRad = setting.rad,
            apiKey = mapApiKey
        )

        call.respond(
            ThymeleafContent(
                "map",
                mapOf("config" to mapConfig)
            )
        )
    }

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
            patch("/edit") {
                withClaim { claim ->
                    val body = call.receive<EditGeneralSettingBody>()
                    val response = service.updateGeneralSetting(body = body, editBy = claim.id)
                    call.respond(status = response.statusCode, message = response.message)
                }
            }
        }
    }

    authenticate("auth-jwt") {
        sse("/sse", serialize = { typeInfo, it ->
            val serializer = Json.serializersModule.serializer(typeInfo.kotlinType!!)
            Json.encodeToString(serializer, it)
        }) {
            heartbeat {
                period = 10.milliseconds
                event = ServerSentEvent("heartbeat")
            }

            launch {
                eventBus.events.collect {
                    val data = mapper.mapGetGeneralSettingResponse(it)
                    try {
                        send(data = data, event = "global-setting")
                        send(event = "global-setting-update")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    println("general-setting sent")
                }
            }
        }
    }
}