package com.rollinup.server.route.test

import com.rollinup.server.CommonException
import com.rollinup.server.IllegalPathParameterException
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.service.attendance.AttendanceService
import com.rollinup.server.service.email.EmailService
import com.rollinup.server.util.Config
import com.rollinup.server.util.manager.TransactionManagerImpl
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
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Route.testRoute() {

    val emailService by inject<EmailService>()
    val config by inject<Config>()
    val attendanceService by inject<AttendanceService>()

    post("/upload") {
        var fileDescriptions = ""
        var fileName = ""
        val multiPartData = call.receiveMultipart(formFieldLimit = 1024 * 1024 * 100)

        multiPartData
            .forEachPart { part ->
            when (part) {
                is PartData.FormItem -> {
//                    fileDescriptions = part
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

        val duration = Duration.between(instantJava,instantJava).toHours()
    }

    get("/attendance"){
        val queryParams = AttendanceQueryParams(
            limit = call.queryParameters["limit"]?.toIntOrNull(),
            page = call.queryParameters["page"]?.toIntOrNull(),
            sortBy = call.queryParameters["sortBy"] ,
            order = call.queryParameters["order"],
            search = call.queryParameters["search"],
            status = call.queryParameters["status"]?.split(","),
            xClass = call.queryParameters["class"]?.split(",")?.map{it.toInt()},
            dateRange = call.queryParameters["dateRange"]?.split(",")?.map { it->
                it.toLong()
            },
            studentId = call.queryParameters["studentId"]
        )

        val response = attendanceService.getAttendance(queryParams)
        call.respond(status = response.statusCode, message = response)
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