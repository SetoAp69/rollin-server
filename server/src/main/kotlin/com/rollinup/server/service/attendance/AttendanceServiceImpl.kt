package com.rollinup.server.service.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.UnauthorizedTokenException
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.mapper.AttendanceMapper
import com.rollinup.server.model.request.attendance.AttendanceQueryParams
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceListResponse
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.successGettingResponse
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import java.io.File
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

class AttendanceServiceImpl(
    private val attendanceRepository: AttendanceRepository,
    private val mapper: AttendanceMapper,
    private val transactionManager: TransactionManager,
) : AttendanceService {
    override suspend fun getAttendance(queryParams: AttendanceQueryParams): Response<GetAttendanceListResponse> =
        transactionManager.suspendTransaction {
            val list = attendanceRepository.getAttendanceList(queryParams)
            val summary = attendanceRepository.getSummary(queryParams)
            val response = mapper.mapAttendanceList(
                query = queryParams,
                data = list,
                summary = summary
            )

            return@suspendTransaction Response(
                status = 200,
                message = "Attendance".successGettingResponse(),
                data = response
            )
        }

    override suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse> =
        transactionManager.suspendTransaction {
            val result = attendanceRepository.getAttendanceById(id)
                ?: throw "Attendance".notFoundException()


            return@suspendTransaction Response(
                status = 200,
                message = "",
                data = mapper.mapAttendanceById(result)
            )

        }

    override suspend fun createAttendanceData(
        multiPartData: MultiPartData,
        studentUserId: String,
    ): Response<Unit> {
        var longitude: Double = 0.0
        var latitude: Double = 0.0
//        var bytes: ByteArray? = null
        var fileName: String = ""

        studentUserId.ifBlank { throw UnauthorizedTokenException() }

        multiPartData.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> {
                    when (partData.name) {
                        "longitude" -> {
                            longitude = partData.value.toDoubleOrNull()
                                ?: throw CommonException(Message.INVALID_REQUEST_BODY)
                        }

                        "latitude" -> {
                            latitude = partData.value.toDoubleOrNull()
                                ?: throw CommonException(Message.INVALID_REQUEST_BODY)
                        }
                    }
                }

                is PartData.FileItem -> {
//                    bytes = partData.provider().readByteArray()
                    val date = OffsetDateTime.ofInstant(Instant.now(), ZoneOffset.UTC)
                    fileName =
                        "attachment-$studentUserId-${System.currentTimeMillis()}." + partData.originalFileName?.substringAfterLast(
                            "."
                        )
                    val path = Utils.getUploadDir("attachment/attendance/$date/fileName")
                    val file = File(path).apply { parentFile?.mkdirs() }
                    partData.provider().copyAndClose(file.writeChannel())
                }

                else -> {}
            }
            partData.dispose()
        }

        val body = CreateAttendanceBody(
            studentUserId = studentUserId,
            location = CreateAttendanceBody.Location(
                latitude = latitude,
                longitude = longitude
            )
        )

        return transactionManager.suspendTransaction {
            attendanceRepository.createAttendanceData(body = body)

            Response(
                status = 201,
                message = "Attendance".successCreateResponse(),
                data = null
            )
        }
    }


}