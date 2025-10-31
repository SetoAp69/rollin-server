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
import java.io.File
import java.time.LocalTime
import java.time.OffsetDateTime

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
    override suspend fun getAttendanceById(id: String): Response<GetAttendanceByIdResponse> =
        transactionManager.suspendTransaction {
            val result = attendanceRepository.getAttendanceById(id)
                ?: throw "attendance".notFoundException()

            return@suspendTransaction Response(
                status = 200,
                message = "attendance".successGettingResponse(),
                data = mapper.mapAttendanceById(result)
            )
        }

    /**
     * Creates a new attendance record for a student.
     * This method handles multipart form data containing attendance information and an attachment.
     * It performs validation for location, time, and existing records before creating the new attendance data.
     *
     * @param multiPartData The multipart data containing the attendance request and attachment.
     * @param userId The user ID of the student.
     * @param role The role of the user creating the attendance.
     * @return A [Response] indicating the result of the operation.
     * @throws UnauthorizedTokenException if the student user ID is blank.
     * @throws CommonException if the request is invalid or an error occurs during processing.
     * @throws IllegalLocationException if the student's location is outside the allowed radius.
     */
    override suspend fun createAttendanceData(
        userId: String,
        role: Role,
        formHashMap: HashMap<String, String>,
        fileHashMap: HashMap<String, File>,
    ): Response<Unit> {

        val formHash: HashMap<String, String> = formHashMap
        val fileHash: HashMap<String, File> = fileHashMap

        userId.ifBlank { throw UnauthorizedTokenException() }

        fileHash.ifEmpty { throw "attendance attachment".uploadFileException() }
        formHash.ifEmpty { throw CommonException(Message.INVALID_REQUEST_BODY) }

        val checkInAt = OffsetDateTime.now(Utils.getOffset())

        if (checkInAt.isWeekend() || checkInAt.toLocalDate() in holidayCache.get())
            throw CommonException(Message.OUTSIDE_TIME_PERIOD)

        formHash["checkedInAt"] = checkInAt.toInstant().toEpochMilli().toString()
        formHash["status"] = getAttendanceStatus(checkInAt.toOffsetTime().toLocalTime()).value

        val file = fileHash["attachment"]
            ?: throw "attendance attachment".uploadFileException()

        val body = CreateAttendanceBody.fromHashMap(formHash)

        if (role == Role.STUDENT && userId != body.studentUserId) {
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
                throw "attendance".isExistException()

        }

        val filePath = Utils.getUploadDir(Constant.ATTENDANCE_FILE_PATH, file.name)
        val upload = fileService.uploadFile(filePath, file)

        transactionManager.suspendTransaction {
            attendanceRepository.createAttendanceData(
                body = body.copy(attachment = upload)
            )
            return@suspendTransaction Response(
                status = 201,
                message = "attendance".successCreateResponse(),
                data = null
            )
        }

        return Response(
            status = 201,
            message = "attendance".successCreateResponse(),
            data = null
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

        val dateRange = queryParams.date?.let {
            listOf(it, it)
        }

        val summary = attendanceRepository.getSummary(classKey = classKey, dateRange = dateRange)

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
            message = "attendance".successGettingResponse(),
            data = result
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
        val body = EditAttendanceBody.fromHashMap(formHash)
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
