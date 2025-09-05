package com.rollinup.server.socket

import com.rollinup.server.model.Task
import com.rollinup.server.model.TaskRepository
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.receiveDeserialized
import io.ktor.server.websocket.sendSerialized
import io.ktor.server.websocket.timeout
import io.ktor.server.websocket.webSocket
import io.ktor.websocket.CloseReason
import io.ktor.websocket.close
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.util.Collections
import kotlin.time.Duration.Companion.seconds

fun Application.configureSocket() {
    install(WebSockets) {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }


    routing {
        val sessions =
            Collections.synchronizedList<WebSocketServerSession>(ArrayList())

        webSocket(path = "/task") {
            sendAllTask()
            close(CloseReason(CloseReason.Codes.NORMAL, "All done"))
        }

        webSocket(path = "/task2") {
            sessions.add(this)
            sendAllTask()

            while (true) {
                val newTask = receiveDeserialized<Task>()

                TaskRepository.addTask(newTask)

                for (session in sessions) {
                    session.sendSerialized(newTask)
                }
            }
        }


    }
}

private suspend fun DefaultWebSocketServerSession.sendAllTask() {
    val list = TaskRepository.allTasks()

    for (task in list) {
        sendSerialized(data = task)
        delay(1000)
    }
}