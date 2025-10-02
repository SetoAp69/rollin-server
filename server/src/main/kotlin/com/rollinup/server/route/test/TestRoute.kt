package com.rollinup.server.route.test

import com.rollinup.server.CommonException
import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.util.Config
import com.rollinup.server.service.email.EmailService
import io.ktor.client.content.LocalFileContent
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Route.testRoute() {

    val emailService by inject<EmailService>()
    val config by inject<Config>()

    post("/upload") {
        var fileDescriptions = ""
        var fileName = ""
        val multiPartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

        multiPartData.forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
                    fileDescriptions = part.value
                }

                is PartData.FileItem -> {
                    fileName = part.originalFileName ?: return@forEachPart
                    val filePath = config.getUploadDir("/pic/$fileName")
                    val file = File(filePath).apply { parentFile?.mkdirs() }
                    println("\n\n ${file.absolutePath}")
                    part.provider().copyAndClose(file.writeChannel())
                }

                else -> {}
            }
            part.dispose()
        }
    }

    get("/pic/{path}") {
        val path = call.parameters["path"] ?: throw IllegalPathParameterException("path")
        val filePath = config.getUploadDir("pic/$path")
        val file = File(filePath)

        if (file.exists()) {
            call.respond(LocalFileContent(file))
        } else {
            throw CommonException("can't find : $path")
        }

    }
}