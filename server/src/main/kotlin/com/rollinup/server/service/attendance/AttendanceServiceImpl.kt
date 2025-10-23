package com.rollinup.server.service.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.UnauthorizedTokenException
import com.rollinup.server.datasource.api.model.ApiResponse
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.AttendanceMapper
import com.rollinup.server.model.PermitType
import com.rollinup.server.model.request.attendance.AttendanceSummaryQueryParams
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.request.permit.CreatePermitBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse
import com.rollinup.server.service.file.FileService
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.generalsetting.GeneralSettingCache
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse
import com.rollinup.server.util.uploadFileException
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.util.cio.writeChannel
import io.ktor.utils.io.copyAndClose
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.util.UUID

class AttendanceServiceImpl(
    private val attendanceRepository: AttendanceRepository,
    private val permitRepository: PermitRepository,
    private val fileService: FileService,
    private val mapper: AttendanceMapper,
    private val transactionManager: TransactionManager,
    private val generalSetting: GeneralSettingCache,
) : AttendanceService {
    override suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse> =
        transactionManager.suspendTransaction {
            val result = attendanceRepository.getAttendanceById(id)
                ?: throw "Attendance".notFoundException()

            return@suspendTransaction Response(
                status = 200,
                message = "Attendance".successGettingResponse(),
                data = mapper.mapAttendanceById(result)
            )
        }

//    override suspend fun createAttendanceData(
//        multiPartData: MultiPartData,
//        studentUserId: String,
//    ): Response<Unit> {
//
//        val hashMap: HashMap<String, String> = hashMapOf()
//        var cacheDir = ""
//        var uploadPath = ""
//        var file: File? = null
//
//
//        studentUserId.ifBlank { throw UnauthorizedTokenException() }
//        hashMap["studentUserId"] = studentUserId
//
//
//        multiPartData.forEachPart { partData ->
//            when (partData) {
//                is PartData.FormItem -> {
//                    partData.name?.let { hashMap[it] = partData.value }
//                }
//
//                is PartData.FileItem -> {
//                    val fileName =
//                        "$studentUserId-${UUID.randomUUID()}." + partData.originalFileName?.substringAfterLast(
//                            "."
//                        )
//                    cacheDir =
//                        Utils.getCacheDir(path = Constant.ATTENDANCE_FILE_PATH, fileName = fileName)
//
//                    uploadPath = Utils.getUploadDir(
//                        path = Constant.ATTENDANCE_FILE_PATH
//                    )
//
//                    file = File(cacheDir).apply { parentFile?.mkdirs() }
//
//                    withContext(Dispatchers.IO) {
//                        partData.provider().copyAndClose(file.writeChannel())
//                    }
//                    partData.dispose()
//                    return@forEachPart
//                }
//
//                else -> {}
//            }
//            partData.dispose()
//        }
//
//        if (file == null) {
//            throw "attendance attachment".uploadFileException()
//        } else {
//            try {
//                val upload = fileService.uploadFile(filePath = uploadPath, file = file)
//                when (upload) {
//                    is ApiResponse.Error -> {
//                        throw "Attendance attachment".uploadFileException()
//                    }
//
//                    is ApiResponse.Success -> {
//                        hashMap["attachment"] = upload.data
//                        val body = CreateAttendanceBody.fromHashMap(hashMap)
//
//                        transactionManager.suspendTransaction {
//                            attendanceRepository.createAttendanceData(
//                                body = body
//                            )
//                        }
//                    }
//                }
//
//            } finally {
//                file.delete()
//            }
//        }
//
//        return Response(
//            status = 201,
//            message = "Attendance".successCreateResponse(),
//            data = null
//        )
//
//    }

    override suspend fun createAttendanceData(
        multiPartData: MultiPartData,
        studentUserId: String,
    ): Response<Unit> {

        val formHash: HashMap<String, String> = hashMapOf()
        val fileHash: HashMap<String, File> = hashMapOf()

        studentUserId.ifBlank { throw UnauthorizedTokenException() }

        multiPartData.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> fetchFormData(partData, formHash)

                is PartData.FileItem -> fetchFileData(partData, fileHash)

                else -> {}
            }
        }

        fileHash.ifEmpty { throw "attendance attachment".uploadFileException() }

        val checkInAt = OffsetDateTime.now(Utils.getOffset())

        formHash["checkedInAt"] = checkInAt.toInstant().toEpochMilli().toString()
        formHash["status"] = getAttendanceStatus(checkInAt.toOffsetTime()).value

        val body = CreateAttendanceBody.fromHashMap(formHash)

        try {
            val path = Utils.getUploadDir(Constant.ATTENDANCE_FILE_PATH)
            val file = fileHash["attachment"]
                ?: throw "attendance attachment".uploadFileException()

            val upload = fileService.uploadFile(filePath = path, file = file)

            when (upload) {
                is ApiResponse.Error -> {
                    throw "attendance attachment".uploadFileException()
                }

                is ApiResponse.Success -> {
                    attendanceRepository.createAttendanceData(
                        body = body.copy(attachment = upload.data)
                    )
                }
            }
        } finally {
            fileHash.forEach { _, file ->
                file.delete()
            }
        }

        return Response(
            status = 201,
            message = "Attendance".successCreateResponse(),
            data = null
        )
    }

    override suspend fun getAttendanceListByStudent(
        queryParams: GetAttendanceByStudentQueryParams,
        studentId: String,
    ): Response<GetAttendanceByStudentListResponse> = transactionManager.suspendTransaction {
        val summaryQueryParams = AttendanceSummaryQueryParams(
            studentUserId = studentId,
            dateRange = queryParams.dateRange
        )
        val summary = attendanceRepository.getSummary(
            queryParams = summaryQueryParams,
        )

        val attendanceList = attendanceRepository.getAttendanceListByStudent(
            queryParams = queryParams,
            studentId = studentId
        )

        val result = mapper.mapAttendanceListByStudent(
            data = attendanceList,
            summary = summary,
            queryParams = queryParams
        )

        return@suspendTransaction Response(
            status = 200,
            message = "Attendance".successGettingResponse(),
            data = result
        )

    }

    override suspend fun getAttendanceListByClass(
        queryParams: GetAttendanceByClassQueryParams,
        classKey: Int,
    ): Response<GetAttendanceByClassListResponse> = transactionManager.suspendTransaction {
        val summaryQueryParams = AttendanceSummaryQueryParams(
            classX = classKey
        )
        val summary = attendanceRepository.getSummary(
            queryParams = summaryQueryParams,
        )

        val attendanceList = attendanceRepository.getAttendanceListByClass(
            queryParams = queryParams,
            classKey = classKey
        )

        val result = mapper.mapAttendanceListByClass(
            data = attendanceList,
            summary = summary,
            queryParams = queryParams
        )

        return@suspendTransaction Response(
            status = 200,
            message = "Attendance".successGettingResponse(),
            data = result
        )

    }

    override suspend fun updateAttendance(
        id: String,
        editBy: String,
        multiPartData: MultiPartData,
    ): Response<Unit> {
        val formHash: HashMap<String, String> = hashMapOf()
        val fileHash: HashMap<String, File> = hashMapOf()

        multiPartData.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> fetchFormData(partData, formHash)
                is PartData.FileItem -> fetchFileData(partData, fileHash)
                else -> {}
            }
        }

        val type = formHash["status"]?.let {
            AttendanceStatus.fromValue(it)
        }

        when (type) {
            AttendanceStatus.ALPHA -> handleUpdateAlpha(id)
            AttendanceStatus.ABSENT, AttendanceStatus.EXCUSED -> handleUpdateWithPermit(
                type = if (type == AttendanceStatus.ABSENT) PermitType.ABSENCE else PermitType.DISPENSATION,
                formHash = formHash,
                fileHash = fileHash
            )

            AttendanceStatus.CHECKED_IN, AttendanceStatus.LATE -> handleUpdateCheckIn(
                id = id,
                formHash = formHash
            )

            else -> throw CommonException(Message.INVALID_REQUEST_BODY)
        }

        return Response(
            status = 201,
            message = "Attendance".successEditResponse(),
        )
    }


    private fun fetchFormData(
        partData: PartData.FormItem,
        hashMap: HashMap<String, String>,
    ) {
        partData.name?.let {
            hashMap[it] = partData.value
        }
    }

    private suspend fun fetchFileData(
        partData: PartData.FileItem,
        hashMap: HashMap<String, File>,
        customName: String = "",
    ) {
        withContext(Dispatchers.IO) {
            partData.name?.let {
                val fileName = partData.originalFileName.formatFileName(customName)
                val cacheDir =
                    Utils.getCacheDir(path = Constant.UPDATE_FILE_PATH, fileName = fileName)
                val cache = File(cacheDir).apply { parentFile?.mkdirs() }
                partData.provider().copyAndClose(cache.writeChannel())

                hashMap[it] = cache
            }
        }
    }

    private suspend fun handleUpdateCheckIn(
        id: String,
        formHash: HashMap<String, String>,
    ) {
        val body = EditAttendanceBody.fromHashMap(formHash)
        transactionManager.suspendTransaction {
            attendanceRepository.updateAttendanceData(id, body)
            attendanceRepository.updatePermit(id, null)
        }
    }

    private suspend fun handleUpdateWithPermit(
        type: PermitType,
        formHash: HashMap<String, String>,
        fileHash: HashMap<String, File>,
    ) {
        val permitBody = CreatePermitBody.fromHashMap(formHash)
        val file = fileHash["attachment"] ?: throw "permit attachment".uploadFileException()

        try {
            val filePath = Utils.getUploadDir(Constant.PERMIT_FILE_PATH)
            val upload = fileService.uploadFile(
                filePath = filePath,
                file = file
            )

            when (upload) {
                is ApiResponse.Error -> {
                    throw "permit attachment".uploadFileException()
                }

                is ApiResponse.Success -> {
                    transactionManager.suspendTransaction {
                        val permitId = permitRepository.createPermit(permitBody)
                        attendanceRepository.createAttendanceFromPermit(
                            permitId = permitId,
                            duration = permitBody.duration,
                            studentId = permitBody.studentId,
                            status = when (type) {
                                PermitType.DISPENSATION -> AttendanceStatus.EXCUSED
                                PermitType.ABSENCE -> AttendanceStatus.ABSENT
                            }
                        )
                    }
                }
            }
        } finally {
            fileHash.forEach { _, file ->
                file.delete()
            }
        }

    }

    private suspend fun handleUpdateAlpha(id: String) {
        transactionManager.suspendTransaction {
            attendanceRepository.deleteAttendanceData(listOf(id))
        }
    }

    private fun String?.formatFileName(customName: String): String {
        val format = this?.substringAfterLast(".") ?: ""
        return "$customName-${UUID.randomUUID()}.$format".trimEnd('.')
    }

    private fun getAttendanceStatus(
        checkInTime: OffsetTime,
    ): AttendanceStatus {

        val setting = GeneralSettingCache().get()

        val status = when {
            checkInTime.isAfter(setting.checkInPeriodStart) && checkInTime.isBefore(setting.schoolPeriodStart) -> {
                AttendanceStatus.CHECKED_IN
            }

            checkInTime.isBefore(setting.checkInPeriodEnd) -> {
                AttendanceStatus.LATE
            }

            else -> throw CommonException(Message.OUTSIDE_TIME_PERIOD)

        }

        return status
    }


}