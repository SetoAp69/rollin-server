package com.rollinup.server.plugins

import com.rollinup.server.model.Priority
import com.rollinup.server.model.TaskRepository
import com.rollinup.server.util.Utils.taskAsTable
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.contentType
import io.ktor.server.routing.get
import io.ktor.server.routing.routing

fun Application.configureRouting() {

    val textHtmlType = ContentType.parse("text/html")
    routing{
    }
    routing {
        get("/"){

        }

        get("/tasks") {
            call.respondText(
                contentType = ContentType.parse("text/html"),
                text = TaskRepository.allTasks().taskAsTable()
            )
        }

        get("/task/byPriority/{priority?}"){
            val priorityParams = call.parameters["priority"]
            if(priorityParams == null){
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            try{
                val priority = Priority.valueOf(priorityParams)
                val tasks = TaskRepository.taskByPriority(priority)

                if(tasks.isEmpty()){
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respondText (
                    contentType  = textHtmlType,
                    text = tasks.taskAsTable()
                )
            } catch (e: IllegalArgumentException){
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }

}