package com.rollinup.server.service.file

import com.rollinup.server.datasource.api.model.ApiResponse
import java.io.File

interface FileService {
    suspend fun uploadFile(filePath: String, file: File): ApiResponse<String>

    suspend fun deleteFile(filePath: String, file: File): ApiResponse<Unit>
}