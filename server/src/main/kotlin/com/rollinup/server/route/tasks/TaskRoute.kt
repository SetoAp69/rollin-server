package com.rollinup.server.route.tasks

import com.rollinup.server.datasource.database.repository.task.TaskRepository
import com.rollinup.server.model.Priority
import com.rollinup.server.model.Task
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject


fun Route.taskRoute() {
    val repository by inject<TaskRepository>()

    authenticate("auth-jwt") {
        get {
            val searchQuery = call.request.queryParameters["search"]
            val tasks = if (searchQuery.isNullOrBlank()) {
                repository.getTask()
            } else {
                repository.search(searchQuery)
            }
            call.respond(tasks)
        }

        get("/byName/{taskName}") {
            val name = call.parameters["taskName"]
            val task = if (name.isNullOrBlank()) {
                repository.getTask()
            } else {
                repository.getTaskByName(name)
            }

            call.respond(task)
        }

        get("/byPriority/{priority}") {
            val sPriority = call.parameters["priority"]

            if (sPriority.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            try {
                val priority = Priority.fromValue(sPriority)
                val task = repository.getTaskByPriority(priority)

                call.respond(task)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post {
            try {
                val task = call.receive<Task>()
                repository.addTask(task)
                call.respond(HttpStatusCode.Created)

            } catch (e: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        delete("/{taskName}") {
            val nameParams = call.parameters["taskName"]

            if (nameParams.isNullOrBlank()) {
                call.respond(HttpStatusCode.BadRequest)
                return@delete
            }

            repository.removeTask(nameParams)
            call.respond(HttpStatusCode.Accepted)
        }
    }
}