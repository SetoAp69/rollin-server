package com.rollinup.server.route.file

import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.service.file.FileService
import com.rollinup.server.util.Utils
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject

fun Route.fileRoute() {
    val fileService by inject<FileService>()
    authenticate("auth-jwt") {
        get() {
            val filePath = call.receive<HashMap<String, String>>()["path"]
                ?.let {
                    it.ifBlank { null }
                }
                ?: throw IllegalPathParameterException("path")

            val contentType = Utils.getContentType(filePath.substringAfterLast("."))
            val file = fileService.getFileUrl(filePath)
            call.respondBytes(bytes = file, contentType = contentType)
        }
    }
}