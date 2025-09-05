package com.rollinup.server

import com.rollinup.server.configurations.module
import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.converter
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.deserialize
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `Get Task By Priority should return task with given priority`() = testApplication {
        //Arrange
        application {
            module()
        }
        val clients = client.config {
            install(ContentNegotiation) {
                json()
            }
        }

        //Act
        val response = clients.get("/task/byPriority/LOW")
        val result = response.body<List<Task>>()

        //Assert
        assertEquals(expected = HttpStatusCode.OK, actual = response.status)
    }

    @Test
    fun `Get Task By Priority should return 400 with given invalid priority`() = testApplication {
        //Arrange
        application {
            module()
        }
        val clients = client.config {
            install(ContentNegotiation) {
                json()
            }
        }

        //Act
        val response = clients.get("/task/byPriority/INVALID")

        //Assert
        assertEquals(expected = HttpStatusCode.BadRequest, actual = response.status)
    }

    @Test
    fun `Get Task By Priority should return 404 if there's no data to be sent`() = testApplication {
        //Arrange
        application {
            module()
        }
        val clients = client.config {
            install(ContentNegotiation) {
                json()
            }
        }

        //Act
        val response = clients.get("/task/byPriority/VITAL")

        //Assert
        assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
    }

    @Test
    fun `Post new task should return 201 if success`() = testApplication {
        application {
            module()
        }

        val clients = client.config {
            install(ContentNegotiation) {
                json()
            }
        }

        val body = Task(
            name = "test",
            descriptions = "desc",
            priority = Priority.LOW
        )

        //Act
        val response = clients.post(("/task")) {
            header(
                HttpHeaders.ContentType,
                ContentType.Application.Json
            )
            setBody(body)
        }

        val getResponse = clients.get("/task")

        assertTrue { response.status.isSuccess() }
        assertTrue { response.status == HttpStatusCode.Created }
        assertTrue { getResponse.body<List<Task>>().isNotEmpty() }
    }


    @Test
    fun `Delete task should return 201 if success`() = testApplication {
        application {
            module()
        }

        val clients = client.config {
            install(ContentNegotiation) {
                json()
            }
        }

        val name = "painting"

        //Act
        val response = clients.delete(("/task/$name"))

        assertTrue { response.status.isSuccess() }
        assertEquals(expected = HttpStatusCode.Accepted, actual = response.status)
    }

    @Test
    fun testRoot() = testApplication {
        application {
            module()
        }

        val client = client.config {
            install(ContentNegotiation) {
                json()
            }

            install(WebSockets) {
                contentConverter = KotlinxWebsocketSerializationConverter(Json)
            }
        }

        val expectedTask = listOf(
            Task("cleaning", "Clean the house", Priority.LOW),
            Task("gardening", "Mow the lawn", Priority.MEDIUM),
            Task("shopping", "Buy the groceries", Priority.HIGH),
            Task("painting", "Paint the fence", Priority.MEDIUM)
        )

        //Act
        var actualTask = emptyList<Task>()

        client.webSocket("/task") {
            consumeTaskAsFlow().collect { tasks ->
                actualTask = tasks
            }
        }

        //Assert
        assertEquals(expected = expectedTask, actual = actualTask)
    }

    private fun DefaultClientWebSocketSession.consumeTaskAsFlow() = incoming
        .consumeAsFlow()
        .map {
            converter!!.deserialize<Task>(it)
        }
        .scan(initial = emptyList<Task>()) { list, task ->
            list + task
        }

}

