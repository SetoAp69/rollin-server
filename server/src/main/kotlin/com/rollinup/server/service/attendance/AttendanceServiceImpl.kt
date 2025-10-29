package com.rollinup.server.service.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.IllegalLocationException
import com.rollinup.server.UnauthorizedTokenException
import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.holiday.HolidayCache
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.PermitType
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.AttendanceMapper
import com.rollinup.server.model.Role
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
import com.rollinup.server.util.Utils.isWeekend
import com.rollinup.server.util.Utils.toLocalDate
import com.rollinup.server.util.isExistException
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse
import com.rollinup.server.util.uploadFileException
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import java.io.File
import java.time.LocalTime
import java.time.OffsetDateTime

class AttendanceServiceImpl(
    private val attendanceRepository: AttendanceRepository,
    private val permitRepository: PermitRepository,
    private val fileService: FileService,
    private val mapper: AttendanceMapper,
    private val transactionManager: TransactionManager,
    private val generalSetting: GeneralSettingCache,
    private val holidayCache: HolidayCache,
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

    override suspend fun createAttendanceData(
        multiPartData: MultiPartData,
        studentUserId: String,
        role: Role,
    ): Response<Unit> {

        val formHash: HashMap<String, String> = hashMapOf()
        val fileHash: HashMap<String, File> = hashMapOf()

        studentUserId.ifBlank { throw UnauthorizedTokenException() }

        multiPartData.forEachPart { partData ->
            when (partData) {
                is PartData.FormItem -> Utils.fetchFormData(partData, formHash)

                is PartData.FileItem -> Utils.fetchFileData(partData, fileHash)

                else -> {}
            }
        }

        fileHash.ifEmpty { throw "attendance attachment".uploadFileException() }

        val checkInAt = OffsetDateTime.now(Utils.getOffset())

        if (checkInAt.isWeekend() || checkInAt.toLocalDate() in holidayCache.get())
            throw CommonException(Message.OUTSIDE_TIME_PERIOD)

        formHash["checkedInAt"] = checkInAt.toInstant().toEpochMilli().toString()
        formHash["status"] = getAttendanceStatus(checkInAt.toOffsetTime().toLocalTime()).value

        val body = CreateAttendanceBody.fromHashMap(formHash)

        if (role == Role.STUDENT && studentUserId != body.studentUserId) {
            throw IllegalArgumentException("studentId")
        }

        val isLocationValid = Utils.validateLocations(
            locationA = generalSetting.get().lat to generalSetting.get().long,
            locationB = body.latitude to body.longitude,
            rad = generalSetting.get().rad
        )

        if (!isLocationValid) throw IllegalLocationException()
        val currentDate = OffsetDateTime.now().toInstant().toEpochMilli()
        val queryParams = GetAttendanceByStudentQueryParams(
            dateRange = listOf(
                currentDate, currentDate
            )
        )
        transactionManager.suspendTransaction {
            val attendance = attendanceRepository.getAttendanceListByStudent(
                queryParams = queryParams,
                studentId = body.studentUserId
            )

            if (attendance.isNotEmpty())
                throw "attendance data".isExistException()

        }

        val file = fileHash["attachment"]
            ?: throw "attendance attachment".uploadFileException()
        val filePath = Utils.getUploadDir(Constant.ATTENDANCE_FILE_PATH, file.name)

        val upload = fileService.uploadFile(filePath, file)

        transactionManager.suspendTransaction {
            attendanceRepository.createAttendanceData(
                body = body.copy(attachment = upload)
            )
            return@suspendTransaction Response(
                status = 201,
                message = "Attendance".successCreateResponse(),
                data = null
            )
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

        val summary = attendanceRepository.getSummary(
            studentId = studentId,
            dateRange = queryParams.dateRange
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

        val summary = attendanceRepository.getSummary()

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
                is PartData.FormItem -> Utils.fetchFormData(partData, formHash)
                is PartData.FileItem -> Utils.fetchFileData(partData, fileHash)
                else -> {}
            }
        }

        val type = formHash["status"]?.let {
            AttendanceStatus.fromValue(it)
        }

        when (type) {
            AttendanceStatus.ALPHA -> handleUpdateAlpha(id)
            AttendanceStatus.ABSENT, AttendanceStatus.EXCUSED -> handleUpdateWithPermit(
                id = id,
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


    private suspend fun handleUpdateCheckIn(
        id: String,
        formHash: HashMap<String, String>,
    ) {
        val body = EditAttendanceBody.fromHashMap(formHash)
        transactionManager.suspendTransaction {
            attendanceRepository.updateAttendanceData(listOf(id), body)
            attendanceRepository.updatePermit(id, null)
        }
    }

    private suspend fun handleUpdateWithPermit(
        id: String,
        type: PermitType,
        formHash: HashMap<String, String>,
        fileHash: HashMap<String, File>,
    ) {
        val file = fileHash["attachment"] ?: throw "permit attachment".uploadFileException()

        val path = Utils.getUploadDir(Constant.PERMIT_FILE_PATH, file.name)
        val upload = fileService.uploadFile(
            filePath = path,
            file = file
        )

        transactionManager.suspendTransaction {
            val studentId = attendanceRepository
                .getAttendanceById(id)?.let {
                    it.student.id.ifBlank { null }
                } ?: throw "student id".notFoundException()

            formHash["studentId"] = studentId
            formHash["type"] = type.value

            val permitBody = CreatePermitBody.fromHashMap(formHash)

            val permitId = permitRepository.createPermit(
                permitBody.copy(
                    studentId = studentId,
                    attachment = upload
                )
            )

            val duration = permitBody.duration.map { it.toLocalDate() }

            val dates = Utils.generateDateRange(
                start = duration.first(),
                end = duration.last()
            ).filter {
                !it.isWeekend() && it !in holidayCache.get()
            }


            attendanceRepository.createAttendanceFromPermit(
                permitId = permitId,
                studentId = permitBody.studentId,
                dates = dates,
                status = when (type) {
                    PermitType.DISPENSATION -> AttendanceStatus.EXCUSED
                    PermitType.ABSENCE -> AttendanceStatus.ABSENT
                }
            )
        }

    }

    private suspend fun handleUpdateAlpha(id: String) {
        transactionManager.suspendTransaction {
            attendanceRepository.deleteAttendanceData(listOf(id))
        }
    }


    private fun getAttendanceStatus(
        checkInTime: LocalTime,
    ): AttendanceStatus {

        val status = when (checkInTime) {
            in generalSetting.get().checkInPeriodStart..generalSetting.get().schoolPeriodStart -> AttendanceStatus.CHECKED_IN
            in generalSetting.get().schoolPeriodStart..generalSetting.get().checkInPeriodEnd -> AttendanceStatus.LATE
            else -> throw CommonException(Message.OUTSIDE_TIME_PERIOD)
        }

        return status
    }


}