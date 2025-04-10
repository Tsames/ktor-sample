package com.example.tsames

import Priority
import com.example.tsames.model.TaskRepository
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.http.content.staticResources
import tasksAsTable

fun Application.configureRouting() {
    routing {
        staticResources("/task-ui", "task-ui")

        route("/tasks") {
            get {
                val tasks = TaskRepository.allTasks()
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = tasks.tasksAsTable()
                )
            }

            get ("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if (name == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val task = TaskRepository.taskByName(name)
                if (task == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = listOf(task).tasksAsTable()
                )
            }

            get("/byPriority/{priority}") {
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

            post {
                val priorityAsText = call.parameters["priority"]
                // If our query param does not exist, return 400
                if (priorityAsText == null) {
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                // Else Try processing the request
                try {
                    val priority = Priority.valueOf(priorityAsText)
                    val tasks = TaskRepository.tasksByPriority(priority)

                    if (tasks.isEmpty()) {
                        call.respond(HttpStatusCode.NotFound)
                        return@post
                    }

                    call.respondText(contentType = ContentType.parse("text/html"), text = tasks.tasksAsTable())
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
