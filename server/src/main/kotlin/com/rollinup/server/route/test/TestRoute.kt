package com.rollinup.server.route.test

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageOptions
import com.rollinup.server.CommonException
import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.service.attendance.AttendanceService
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.util.Config
import com.rollinup.server.util.Utils
import com.rollinup.server.util.manager.TransactionManagerImpl
import io.ktor.client.content.LocalFileContent
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.koin.ktor.ext.inject
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Route.testRoute() {

    val emailService by inject<EmailService>()
    val config by inject<Config>()
    val attendanceService by inject<AttendanceService>()

    post("/upload") {
        val projectId = "bubbly-granite-475802-v4"
        val bucketName = "rollin_up_server"

        val storage = StorageOptions
            .newBuilder()
            .setProjectId(projectId)
            .build()
            .service

        var cache: File = File("")
        var name: String = ""

        val multiPart = call.receiveMultipart()
        multiPart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    val fileName = part.originalFileName as String
                    val cacheDir = Utils.getCacheDir("cache", fileName)
                    name = part.originalFileName as String
                    cache = File(cacheDir).apply { parentFile?.mkdirs() }
                    part.provider().copyAndClose(cache.writeChannel())
                }

                else -> ""
            }
            part.dispose()
        }

        val blobId = BlobId.of(bucketName, name)
        val blobInfo = BlobInfo.newBuilder(blobId).setContentType("image/jpeg").build()
        try {
            val blob = storage.create(blobInfo, cache.readBytes())
            call.respond(HttpStatusCode.OK, "${blob.mediaLink}")
        } catch (e: Exception) {
            call.respond(status = HttpStatusCode.BadRequest, message = e.toString())
        }

        val x = storage.create(blobInfo, cache.readBytes()).mediaLink ?: ""
    }

    get("/file/{fileName}") {
        val projectId = "bubbly-granite-475802-v4"

        val storage = StorageOptions
            .newBuilder()
            .setProjectId(projectId)
            .build()
            .service

        val fileName = call.pathParameters["fileName"]
            ?: throw IllegalPathParameterException("fileName")
        val blobInfo = BlobInfo.newBuilder("rollin_up_server", fileName).build()


        try {
            val url = storage.signUrl(
                blobInfo,
                600, TimeUnit.SECONDS,
                Storage.SignUrlOption.withV4Signature()
            )
            call.respond(status = HttpStatusCode.OK, message = url.toString())
        } catch (e: Exception) {
            call.respond(status = HttpStatusCode.BadRequest, message = e.toString())

        }

    }

    get {
        staticResources("/end-point-test", "end-point-test") {
            default("index.html")
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

    get("/test-date") {
        val from = call.queryParameters["from"]?.toLong()
        val to = call.queryParameters["to"]?.toLong()


        val currentDateDb = getInRange(from, to)
        val currentDate = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        val offetDateTime = OffsetDateTime.of(
            LocalDateTime.of(
                LocalDate.now(),
                LocalTime.of(7, 0)
            ),
            ZoneOffset.of("+07")
        )


        val message: HashMap<String, Any> = hashMapOf(
//            "System date" to currentDate,
//            "Database date" to currentDateDb,
//            "offset date time of local 07:00" to offetDateTime

            "Database date" to currentDateDb.toString(),
            "Local date time" to currentDateDb.map {
                it.get("time")?.let {
                    kotlin.time.Instant.parse(it)
                }
            }
        )

        val instant = kotlin.time.Instant.parse("")
        val instantJava = Instant.parse("")
        val localDateTimeA = LocalDateTime.now()
        val localDateTimeB = LocalDateTime.now()

        val duration = Duration.between(instantJava, instantJava).toHours()
    }


}

object DummyTable : Table("dummy") {
    val a = varchar("a", 12)
    val b = varchar("b", 12)
    val updatedAt = timestamp("updated_at")
}

val transactionManager = TransactionManagerImpl()

suspend fun getInRange(
    from: Long?,
    to: Long?,
): List<HashMap<String, String>> = transactionManager.suspendTransaction {
    val fromInstant = from.let {
        if (it != null) {
            Instant.ofEpochMilli(it)
        } else {
            null
        }
    }

    val toInstant = to.let {
        if (it != null) {
            Instant.ofEpochMilli(it)
        } else {
            null
        }
    }

    val x = DummyTable
        .selectAll()

    if (from != null && to != null) {
        x.where {
            DummyTable.updatedAt.between(fromInstant, toInstant)
        }
    }

    return@suspendTransaction x.map {
        hashMapOf(
            "a" to it[DummyTable.a],
            "time" to it[DummyTable.updatedAt].toString(),
        )

    }
}
