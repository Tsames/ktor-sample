package com.example.tsames

import Priority
import Task
import com.example.tsames.model.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.*
import tasksAsTable

fun Application.configureRouting() {
    routing {
        staticResources("/task-ui", "task-ui")

        get("/tasks") {
            val tasks = TaskRepository.allTasks()
            call.respondText(
                contentType = ContentType.parse("text/html"),
                text = tasks.tasksAsTable()
            )
        }

        get("/tasks/byPriority/{priority}") {
            val priorityAsText = call.parameters["priority"]
            // If our query param does not exist, return 400
            if (priorityAsText == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            // Else Try processing the request
            try {
                val priority = Priority.valueOf(priorityAsText)
                val tasks = TaskRepository.tasksByPriority(priority)

                if (tasks.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respondText(contentType = ContentType.parse("text/html"), text = tasks.tasksAsTable())
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }

        post("/tasks") {
            val formContent = call.receiveParameters()

            val params = Triple(
                formContent["name"] ?: "",
                formContent["description"] ?: "",
                formContent["priority"] ?: "",
            )

            if (params.toList().any { it.isEmpty() }) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            try {
                val priority = Priority.valueOf((params.third))
                TaskRepository.addTask(
                    Task(
                        params.first,
                        params.second,
                        priority,
                    )
                )

                call.respond(HttpStatusCode.NoContent)
            } catch (ex: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            } catch (ex: IllegalStateException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
