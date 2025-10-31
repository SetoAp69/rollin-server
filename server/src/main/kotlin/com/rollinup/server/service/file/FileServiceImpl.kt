package com.rollinup.server.service.file

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageException
import com.rollinup.server.util.Utils
import com.rollinup.server.util.deleteFileException
import com.rollinup.server.util.getFileException
import com.rollinup.server.util.uploadFileException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

class FileServiceImpl(
    private val googleStorage: GoogleStorage,
) : FileService {

    val storage = googleStorage.storage

    override suspend fun uploadFile(
        filePath: String,
        file: File,
    ): String = withContext(Dispatchers.IO) {
        try {

            val blobId = BlobId.of(googleStorage.bucketName, filePath + file.name)
            val contentType = Utils.getContentType(file.extension).contentType
            val blobInfo = BlobInfo
                .newBuilder(blobId)
                .setContentType(contentType)
                .build()

            val fileName = storage.create(blobInfo, file.readBytes()).name
            return@withContext fileName

        } catch (e: StorageException) {
            throw filePath.uploadFileException()
        } finally {
            file.delete()
        }
    }

    override suspend fun deleteFile(filePath: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val blobId = filePath.map { BlobId.of(googleStorage.bucketName, it) }
                storage.delete(blobId)

            } catch (e: StorageException) {
                throw filePath.last().deleteFileException()
            }
        }
    }

    override suspend fun getFileUrl(file: String): ByteArray = withContext(Dispatchers.IO) {
        try {
            val blobId = BlobId.of(googleStorage.bucketName, file)
            val blob = storage.get(blobId).signUrl(
                600, TimeUnit.SECONDS, Storage.SignUrlOption.withV4Signature()
            )
            return@withContext blob.readBytes()
        } catch (e: StorageException) {
            throw file.getFileException()
        }

    }
}