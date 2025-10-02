//package com.rollinup.server
//
//import com.jayway.jsonpath.DocumentContext
//import com.jayway.jsonpath.JsonPath
//import com.rollinup.server.configurations.module
//import com.rollinup.server.model.Priority
//import io.ktor.client.HttpClient
//import io.ktor.client.request.accept
//import io.ktor.client.request.get
//import io.ktor.client.statement.bodyAsText
//import io.ktor.http.ContentType
//import io.ktor.server.testing.testApplication
//import org.junit.Test
//import kotlin.test.assertEquals
//
//class JsonPathTest {
//    @Test
//    fun `Task can be found`() = testApplication {
//        application {
//            module()
//        }
//
//        val jsonDoc = client.getAsJsonPath("/task")
//
//        val result: List<String> = jsonDoc.read("$[*].name")
//        assertEquals("cleaning", result[0])
//        assertEquals("gardening", result[1])
//    }
//
//    @Test
//    fun `Get Task By Priority should return task with given priority`() =
//        testApplication {
//            application{
//                module()
//            }
//
//            val priority = Priority.LOW
//            val jsonDoc = client.getAsJsonPath("/task/byPriority/$priority")
//
//            val result:List<String> = jsonDoc.read("$[?(@.priority=='$priority')].name")
//
//            assertEquals("cleaning", result[0])
//
//        }
//
//
//    suspend fun HttpClient.getAsJsonPath(url: String): DocumentContext {
//        val response = this.get(url) {
//            accept(ContentType.Application.Json)
//        }
//        return JsonPath.parse(response.bodyAsText())
//    }
//
//}
