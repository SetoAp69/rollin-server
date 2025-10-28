package com.rollinup.server.service.file

import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.rollinup.server.CommonException
import com.rollinup.server.util.Config
import com.rollinup.server.util.Message

class GoogleStorage() {
    val storage: Storage
    val bucketName = Config.getGCSConfig().bucketName
    val projectId = Config.getGCSConfig().projectId

    init {
        storage = StorageOptions
            .newBuilder()
            .setProjectId(projectId)
            .build()
            .service
            ?: throw CommonException(Message.STORAGE_CONNECTION_FAILED)
    }
}