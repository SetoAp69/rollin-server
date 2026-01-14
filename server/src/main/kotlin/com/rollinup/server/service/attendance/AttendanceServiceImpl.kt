package com.rollinup.server.service.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.IllegalLocationException
import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.holiday.HolidayCache
import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.PermitType
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.AttendanceMapper
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import com.rollinup.server.model.request.attendance.CreateAttendanceRequest
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.request.attendance.GetExportAttendanceQueryParams
import com.rollinup.server.model.request.permit.CreatePermitBody
import com.rollinup.server.model.request.permit.EditPermitBody
import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceListSummaryResponse
import com.rollinup.server.model.response.attendance.GetExportAttendanceResponse
import com.rollinup.server.service.file.FileService
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.isWeekend
import com.rollinup.server.util.Utils.toLocalDate
import com.rollinup.server.util.Utils.toLocalTime
import com.rollinup.server.util.illegalStatusExeptions
import com.rollinup.server.util.isExistException
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.missingArgumentException
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse
import com.rollinup.server.util.uploadFileException
import java.io.File
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetTime

/**
 * Service implementation for handling attendance-related operations.
 * This class is responsible for managing student attendance, including creating, retrieving, and updating attendance records.
 * It interacts with repositories, caches, and other services to perform its tasks.
 *
 * @property attendanceRepository The repository for accessing attendance data.
 * @property permitRepository The repository for accessing permit data.
 * @property fileService The service for handling file uploads.
 * @property mapper The mapper for converting data models to response models.
 * @property transactionManager The manager for handling database transactions.
 * @property generalSetting The cache for general application settings.
 * @property holidayCache The cache for holiday information.
 */
class AttendanceServiceImpl(
    private val attendanceRepository: AttendanceRepository,
    private val permitRepository: PermitRepository,
    private val fileService: FileService,
    private val mapper: AttendanceMapper,
    private val transactionManager: TransactionManager,
    private val generalSetting: GeneralSettingCache,
    private val holidayCache: HolidayCache,
) : AttendanceService {
    /**
     * Retrieves attendance data by its unique ID.
     *
     * @param id The unique ID of the attendance record.
     * @return A [Response] containing the [GetAttendanceByIdResponse] if found.
     * @throws CommonException if the attendance record is not found.
     */
    override suspend fun getAttendanceById(
        id: String,
        role: Role,
    ): Response<GetAttendanceByIdResponse> =
        transactionManager.suspendTransaction {
            val result = attendanceRepository.getAttendanceById(id)
                ?: throw "attendance".notFoundException()

            return@suspendTransaction Response(
                status = 200,
                message = "attendance".successGettingResponse(),
                data = mapper.mapAttendanceById(data = result, role = role)
            )
        }

    override suspend fun checkIn(
        id: String,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit> {
        id.ifBlank { throw "user".notFoundException() }
        val isFormValid = validateMultipartBody(fileHashMap, formHashMap, true)
        if (!isFormValid)
            throw CommonException(Message.INVALID_REQUEST_BODY)

        validateDate(LocalDate.parse(formHashMap["date"] ?: ""))

        transactionManager.suspendTransaction {
            makeSureNoAttendanceDuplicate(
                id = id,
                date = LocalDate.parse(formHashMap["date"] ?: "")
            )
        }
        val upload = uploadAttachment(
            hashMap = fileHashMap,
            isPermit = false
        ) ?: throw "Attachment".uploadFileException()

        val localTime = formHashMap["checkInAt"]?.let {
            LocalDateTime
                .ofInstant(Instant.ofEpochMilli(it.toLong()), Utils.getOffset())
                .toLocalTime()
        } ?: throw "checkInAt".missingArgumentException()

        formHashMap["studentUserId"] = id
        formHashMap["status"] =
            getAttendanceStatus(localTime).toString()
        formHashMap["attachment"] = upload

        val body = CreateAttendanceBody.fromHashMap(formHashMap)

        validateLocation(long = body.longitude, lat = body.latitude)

        transactionManager.suspendTransaction {
            attendanceRepository.createAttendanceData(body)
        }

        return Response(
            status = 201,
            message = "Attendance".successCreateResponse(),
            data = Unit
        )
    }

    override suspend fun createAttendanceData(
        requestBody: CreateAttendanceRequest,
    ): Response<Unit> = transactionManager.suspendTransaction {
        validateDate(requestBody.date)
        makeSureNoAttendanceDuplicate(
            id = requestBody.id,
            date = requestBody.date
        )

        val status = requestBody.checkInAt.let {
            val localTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(it), Utils.getOffset())
                .toLocalTime()

            getAttendanceStatus(localTime)
        }

        val body = CreateAttendanceBody(
            studentUserId = requestBody.id,
            attachment = "",
            status = status,
            checkedInAt = requestBody.checkInAt,
            sDate = requestBody.sDate,
            longitude = generalSetting.get().long,
            latitude = generalSetting.get().lat
        )

        attendanceRepository.createAttendanceData(body)

        return@suspendTransaction Response(
            status = 201,
            message = "Attendance".successCreateResponse(),
        )
    }

    /**
     * Retrieves a list of attendance records for a specific student based on query parameters.
     *
     * @param queryParams The query parameters for filtering the attendance list (e.g., date range).
     * @param studentId The ID of the student.
     * @return A [Response] containing the [GetAttendanceByStudentListResponse].
     */
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
            message = "attendance".successGettingResponse(),
            data = result
        )

    }

    override suspend fun getAttendanceListByStudentSummary(
        dateRange: List<Long>?,
        studentId: String,
    ): Response<GetAttendanceListSummaryResponse> = transactionManager.suspendTransaction {
        val summary = attendanceRepository.getSummary(
            studentId = studentId,
            dateRange = dateRange
        )

        return@suspendTransaction Response(
            status = 200,
            message = "attendance summary".successGettingResponse(),
            data = mapper.mapAttendanceSummary(summary)
        )
    }

    /**
     * Retrieves a list of attendance records for a specific class based on query parameters.
     *
     * @param queryParams The query parameters for filtering the attendance list.
     * @param classKey The key of the class.
     * @return A [Response] containing the [GetAttendanceByClassListResponse].
     */
    override suspend fun getAttendanceListByClass(
        queryParams: GetAttendanceByClassQueryParams,
        classKey: Int,
    ): Response<GetAttendanceByClassListResponse> = transactionManager.suspendTransaction {
        val attendanceList = attendanceRepository.getAttendanceListByClass(
            queryParams = queryParams,
            classKey = classKey,
        )

        val result = mapper.mapAttendanceListByClass(
            data = attendanceList,
            queryParams = queryParams
        )

        return@suspendTransaction Response(
            status = 200,
            message = "attendance".successGettingResponse(),
            data = result
        )
    }

    override suspend fun getAttendanceListByClassSummary(
        classKey: Int,
        date: Long,
    ): Response<GetAttendanceListSummaryResponse> =
        transactionManager.suspendTransaction {
            val summary = attendanceRepository.getSummary(
                classKey = classKey,
                dateRange = listOf(date)
            )

            return@suspendTransaction Response(
                status = 200,
                message = "attendance summary".successGettingResponse(),
                data = mapper.mapAttendanceSummary(summary)
            )
        }

    /**
     * Updates an existing attendance record.
     * The update behavior depends on the new status provided in the multipart data.
     *
     * @param id The ID of the attendance record to update.
     * @param editBy The ID of the user performing the update.
     * @param multiPartData The multipart data containing the updated information.
     * @return A [Response] indicating the result of the operation.
     * @throws CommonException if the request is invalid.
     */
    override suspend fun updateAttendance(
        id: String,
        editBy: String,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit> {
        val formHash: HashMap<String, String> = formHashMap
        val fileHash: HashMap<String, File> = fileHashMap


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

    override suspend fun getExportAttendanceData(queryParams: GetExportAttendanceQueryParams): Response<GetExportAttendanceResponse> =
        transactionManager.suspendTransaction {
            val from = queryParams.dateRange?.firstOrNull() ?: Utils.getStartOfMonth()
            val to = queryParams.dateRange?.lastOrNull() ?: Utils.getEndOfMonth()
            val classKey = queryParams.classKey
                ?: throw "class".missingArgumentException()

            val data = attendanceRepository.getExportAttendanceData(
                classKey = classKey,
                dateRange = listOf(from, to)
            )

            return@suspendTransaction Response(
                status = 200,
                message = "Attendance".successGettingResponse(),
                data = mapper.mapExportAttendanceData(from, to, data)
            )
        }


    /**
     * Handles the update of an attendance record to 'CHECKED_IN' or 'LATE'.
     *
     * @param id The ID of the attendance record.
     * @param formHash The hash map containing form data from the request.
     */
    private suspend fun handleUpdateCheckIn(
        id: String,
        formHash: HashMap<String, String>,
    ) {
        var body = EditAttendanceBody.fromHashMap(formHash)
        body.checkInAt?.let {
            val localTime = LocalDateTime
                .ofInstant(Instant.ofEpochMilli(it), Utils.getOffset())
                .toLocalTime()
            println(localTime.toString())
            val status = getAttendanceStatus(localTime)
            body = body.copy(status = status)
        }
        transactionManager.suspendTransaction {
            attendanceRepository.updateAttendanceData(listOf(id), body)
            attendanceRepository.updatePermit(id, null)
        }
    }

    /**
     * Handles the update of an attendance record that requires a permit (e.g., 'ABSENT', 'EXCUSED').
     * This involves creating a new permit and associating it with the attendance.
     *
     * @param id The ID of the attendance record.
     * @param type The type of permit to create.
     * @param formHash The hash map containing form data from the request.
     * @param fileHash The hash map containing file data from the request.
     * @throws CommonException if the permit attachment is missing or student ID is not found.
     */
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

            handleOverlappedPermit(
                studentId = permitBody.studentId,
                start = permitBody.duration.first(),
                end = permitBody.duration.last()
            )

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

    private fun handleOverlappedPermit(
        studentId: String,
        start: Long,
        end: Long,
    ) {
        val overlappingPermits = permitRepository.getOverlappingPendingPermits(
            studentId = studentId,
            startTime = Instant.ofEpochMilli(start),
            endTime = Instant.ofEpochMilli(end)
        )

        val overlappingPermitsId = overlappingPermits.map { it.id }

        if (overlappingPermits.isNotEmpty()) {
            permitRepository.editPermit(
                listId = overlappingPermitsId,
                body = EditPermitBody(
                    approvalStatus = ApprovalStatus.CANCELED
                )
            )
            cancelPermitAndRollbackAttendance(overlappingPermitsId)
        }
    }

    private fun cancelPermitAndRollbackAttendance(listPermitId: List<String>) {
        val permit = permitRepository
            .getPermitList(GetPermitQueryParams(listId = listPermitId))
            .ifEmpty {
                throw "permit".notFoundException()
            }

        if (permit.any() { it.approvalStatus != ApprovalStatus.APPROVAL_PENDING }) {
            throw "permit".illegalStatusExeptions()
        }

        val attendanceList = attendanceRepository.getAttendanceListByPermit(listPermitId)
        val attendanceByPermit = attendanceList.groupBy { it.permit }

        attendanceByPermit.forEach { permit, att ->
            permit ?: return@forEach
            rollBackAttendance(att, permit.type)
        }

    }

    private fun rollBackAttendance(attendanceList: List<AttendanceEntity>, permitType: PermitType) {
        val listAttendanceId = attendanceList.map { it.id }
        when (permitType) {
            PermitType.ABSENCE -> {
                attendanceList.forEach { att ->
                    if (att.checkedInAt != null) {
                        val status = getDispensationStatus(att.checkedInAt.toLocalTime())
                        attendanceRepository.updateAttendanceData(
                            listOf(att.id),
                            EditAttendanceBody(status = status)
                        )
                    } else {
                        attendanceRepository.deleteAttendanceData(listOf(att.id))
                    }
                }
            }

            PermitType.DISPENSATION -> {
                attendanceRepository.deleteAttendanceData(listAttendanceId)
            }
        }
    }

    private fun getDispensationStatus(time: LocalTime?): AttendanceStatus {
        time ?: throw CommonException(Message.INVALID_TIME_FORMAT)
        val offsetTime = OffsetTime.of(time, Utils.getOffset())
        return getAttendanceStatus(offsetTime.toLocalTime())
    }


    /**
     * Handles the update of an attendance record to 'ALPHA' by deleting the record.
     *
     * @param id The ID of the attendance record to delete.
     */
    private suspend fun handleUpdateAlpha(id: String) {
        transactionManager.suspendTransaction {
            attendanceRepository.deleteAttendanceData(listOf(id))
        }
    }

    private fun makeSureNoAttendanceDuplicate(
        id: String,
        date: LocalDate,
    ) {
        val dateLong = date.atStartOfDay().toInstant(Utils.getOffset()).toEpochMilli()
        val queryParams = GetAttendanceByStudentQueryParams(date = dateLong)
        val attendanceData = attendanceRepository.getAttendanceListByStudent(queryParams, id)

        if (attendanceData.isNotEmpty()) {
            throw "attendance".isExistException()
        }
    }

    private fun validateMultipartBody(
        fileHashMap: HashMap<String, File>,
        formHashMap: HashMap<String, String>,
        isFileRequired: Boolean,
    ) = buildList {
        add(formHashMap)
        if (isFileRequired) add(fileHashMap)
    }.all { it.isNotEmpty() }

    private fun validateLocation(
        long: Double,
        lat: Double,
    ) {
        val userLocation = lat to long
        val areaLocation = generalSetting.get().lat to generalSetting.get().long
        val areaRad = generalSetting.get().rad

        val isValid = Utils.validateLocations(userLocation, areaLocation, areaRad)

        if (!isValid)
            throw IllegalLocationException()
    }

    private suspend fun uploadAttachment(
        hashMap: HashMap<String, File>,
        isPermit: Boolean,
    ): String? {
        val file = hashMap["attachment"]

        return file?.let {
            val filePath = Utils.getUploadDir(
                path = if (isPermit) Constant.PERMIT_FILE_PATH else Constant.ATTENDANCE_FILE_PATH,
                fileName = file.name
            )
            fileService.uploadFile(filePath, file)
        }
    }

    private fun validateDate(attendanceDate: LocalDate) {
        val weekends = listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
        val holidays = holidayCache.get()

        if (attendanceDate.dayOfWeek in weekends || attendanceDate in holidays) {
            throw CommonException(Message.OUTSIDE_TIME_PERIOD)
        }
    }


    /**
     * Determines the attendance status based on the check-in time.
     *
     * @param checkInTime The time of check-in.
     * @return The calculated [AttendanceStatus].
     * @throws CommonException if the check-in time is outside the allowed period.
     */
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
