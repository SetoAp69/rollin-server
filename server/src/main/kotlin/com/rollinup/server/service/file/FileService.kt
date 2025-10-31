package com.rollinup.server.service.file

import java.io.File

interface FileService {
    suspend fun uploadFile(filePath: String, file: File): String

    suspend fun deleteFile(filePath: List<String>)

    suspend fun deleteFile(filePath: String): Unit =
        deleteFile(listOf(filePath))

    suspend fun getFileUrl(file:String): ByteArray


}
