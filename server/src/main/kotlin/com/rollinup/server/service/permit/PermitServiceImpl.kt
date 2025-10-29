package com.rollinup.server.service.permit

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.holiday.HolidayCache
import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.PermitType
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.permit.PermitListEntity
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.PermitMapper
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.request.permit.CreatePermitBody
import com.rollinup.server.model.request.permit.EditPermitBody
import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.model.request.permit.PermitApprovalBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.permit.GetPermitByIdResponse
import com.rollinup.server.model.response.permit.GetPermitListByClassResponse
import com.rollinup.server.model.response.permit.GetPermitListByStudentResponse
import com.rollinup.server.service.file.FileService
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.isWeekend
import com.rollinup.server.util.Utils.toLocalDate
import com.rollinup.server.util.Utils.toLocalTime
import com.rollinup.server.util.illegalStatusExeptions
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.notFoundException
import com.rollinup.server.util.successCreateResponse
import com.rollinup.server.util.successDeleteResponse
import com.rollinup.server.util.successEditResponse
import com.rollinup.server.util.successGettingResponse
import com.rollinup.server.util.uploadFileException
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import java.io.File
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime


class PermitServiceImpl(
    private val permitRepository: PermitRepository,
    private val attendanceRepository: AttendanceRepository,
    private val permitMapper: PermitMapper,
    private val transactionManager: TransactionManager,
    private val fileService: FileService,
    private val generalSetting: GeneralSettingCache,
    private val holidayCache: HolidayCache,
) : PermitService {

    override suspend fun getPermitByStudent(
        studentId: String,
        queryParams: GetPermitQueryParams,
    ): Response<GetPermitListByStudentResponse> = transactionManager.suspendTransaction {
        val data = permitRepository.getPermitList(
            queryParams = queryParams,
            studentId = studentId
        )

        val result = permitMapper.mapPermitByStudent(queryParams = queryParams, data = data)

        val response = Response(
            status = 200,
            message = "permit".successGettingResponse(),
            data = result
        )

        return@suspendTransaction response
    }

    override suspend fun getPermitByClass(
        classKey: Int,
        queryParams: GetPermitQueryParams,
    ): Response<GetPermitListByClassResponse> = transactionManager.suspendTransaction {
        val data = permitRepository.getPermitList(
            queryParams = queryParams,
            classKey = classKey
        )
        val result = permitMapper.mapPermitByClass(queryParams = queryParams, data = data)
        val response = Response(
            status = 200,
            message = "permit".successGettingResponse(),
            data = result
        )

        return@suspendTransaction response
    }

    override suspend fun getPermitById(id: String): Response<GetPermitByIdResponse> =
        transactionManager.suspendTransaction {
            val data = permitRepository.getPermitById(id)
                ?: throw "permit".notFoundException()

            val result = permitMapper.mapPermitById(data = data)

            val response = Response(
                status = 200,
                message = "permit".successGettingResponse(),
                data = result
            )

            return@suspendTransaction response
        }

    override suspend fun doApproval(
        approverId: String,
        body: PermitApprovalBody,
    ): Response<Unit> = transactionManager.suspendTransaction {
        val approvalStatus =
            if (body.isApproved ?: false) ApprovalStatus.APPROVED else ApprovalStatus.DECLINED

        val permitList = permitRepository.getPermitList(
            queryParams = GetPermitQueryParams(
                listId = body.listId,
                isActive = true
            ),
        ).ifEmpty { throw "permit".notFoundException() }

        val permitBody = EditPermitBody(
            approvedBy = approverId,
            approvalStatus = approvalStatus,
            approvalNote = body.approvalNote
        )

        permitRepository.editPermit(body.listId, permitBody)

        handleAttendanceOnApproval(
            isApproved = body.isApproved ?: false,
            permitList = permitList
        )

        return@suspendTransaction Response(
            status = 201,
            message = "permit".successEditResponse(),
        )
    }

    override suspend fun createPermit(multiPart: MultiPartData): Response<Unit> {

        val formHash: HashMap<String, String> = hashMapOf()
        val fileHash: HashMap<String, File> = hashMapOf()

        multiPart.forEachPart { partData ->
            when (partData) {
                is PartData.FileItem -> Utils.fetchFileData(partData, fileHash)
                is PartData.FormItem -> Utils.fetchFormData(partData, formHash)
                else -> {}
            }
            partData.dispose()
        }

        if (listOf(formHash, fileHash).any { it.isEmpty() })
            throw IllegalArgumentException()

        val body = CreatePermitBody.fromHashMap(formHash)
        val file = fileHash["attachment"]
            ?: throw "permit attachment".uploadFileException()
        val path = Utils.getUploadDir(path = Constant.PERMIT_FILE_PATH, file.name)

        val upload = fileService.uploadFile(path, file)

        transactionManager.suspendTransaction {
            val permitId = permitRepository.createPermit(body.copy(attachment = upload))

            val duration = body.duration.map { it.toLocalDate() }

            val dates = Utils.generateDateRange(
                start = duration.first(),
                end = duration.last()
            ).filter {
                !it.isWeekend() && it !in holidayCache.get()
            }

            attendanceRepository.createAttendanceFromPermit(
                permitId = permitId,
                studentId = body.studentId,
                dates = dates,
                status = AttendanceStatus.APPROVAL_PENDING
            )
        }

        return Response(
            status = 201,
            message = "permit".successCreateResponse(),
        )
    }

    override suspend fun editPermit(id: String, multiPart: MultiPartData): Response<Unit> {
        val formHash: HashMap<String, String> = hashMapOf()
        val fileHash: HashMap<String, File> = hashMapOf()

        multiPart.forEachPart { partData ->
            when (partData) {
                is PartData.FileItem -> Utils.fetchFileData(partData, fileHash)
                is PartData.FormItem -> Utils.fetchFormData(partData, formHash)
                else -> {}
            }
            partData.dispose()
        }

        val attachment = fileHash["attachment"]
        val body = EditPermitBody.fromHashMap(formHash)

        if (attachment != null) {
            handleEditWithAttachment(
                id = id,
                body = body,
                attachment = attachment
            )
        } else {
            handleEditWithoutAttachment(
                id = id,
                body = body
            )
        }

        return Response(
            status = 201,
            message = "permit".successEditResponse(),
        )
    }

    override suspend fun cancelPermit(id: List<String>): Response<Unit> =
        transactionManager.suspendTransaction {
            val permit =
                permitRepository
                    .getPermitList(GetPermitQueryParams(listId = id))
                    .ifEmpty {
                        throw "permit".notFoundException()
                    }

            if (permit.any { it.approvalStatus != ApprovalStatus.APPROVAL_PENDING }) {
                throw "permit".illegalStatusExeptions()
            }

            val attendanceList = attendanceRepository.getAttendanceListByPermit(id)
            val attendanceListByPermit = attendanceList.groupBy { it.permit }

            attendanceListByPermit.forEach { permit, att ->
                permit ?: return@forEach
                rollBackAttendance(
                    attendanceList = att,
                    permitType = permit.type
                )
            }

            permitRepository.editPermit(
                listId = id,
                body = EditPermitBody(
                    approvalStatus = ApprovalStatus.CANCELED
                )
            )

            return@suspendTransaction Response(
                status = 201,
                message = "permit".successEditResponse(),
            )
        }

    override suspend fun deletePermit(id: List<String>): Response<Unit> {
        val permit = transactionManager.suspendTransaction {
            permitRepository
                .getPermitList(GetPermitQueryParams(listId = id))
                .ifEmpty {
                    throw "permit".notFoundException()
                }
        }

        transactionManager.suspendTransaction {
            permitRepository.deletePermit(id)
        }

        fileService.deleteFile(permit.map { it.attachment })

        return Response(
            status = 200,
            message = "permit".successDeleteResponse(),
        )
    }

    private fun handleAttendanceOnApproval(
        isApproved: Boolean,
        permitList: List<PermitListEntity>,
    ) {
        val attendanceList =
            attendanceRepository.getAttendanceListByPermit(permitList.map { it.id })

        println("PermitList: ${permitList.map { it.id }}")

        val attendanceByPermit = attendanceList.groupBy { it.permit }
        println("Att By Permit: $attendanceByPermit")

        attendanceByPermit.forEach { permit, att ->
            permit ?: return@forEach
            println("att id: ${att.map { it.id }}")

            val id = att.map { it.id }

            if (isApproved) {
                val status =
                    if (permit.type == PermitType.DISPENSATION) AttendanceStatus.EXCUSED else AttendanceStatus.ABSENT

                attendanceRepository.updateAttendanceData(
                    listId = id,
                    body = EditAttendanceBody(status = status)
                )
            } else {
                rollBackAttendance(
                    attendanceList = att,
                    permitType = permit.type
                )
            }
        }
    }

    private fun rollBackAttendance(attendanceList: List<AttendanceEntity>, permitType: PermitType) {
        val attendanceId = attendanceList.map { it.id }
        when (permitType) {
            PermitType.DISPENSATION -> {
                attendanceList.forEach { att ->
                    if (att.checkedInAt != null) {
                        val status = getDispensationStatus(
                            time = att.checkedInAt.toLocalTime()
                        )

                        attendanceRepository.updateAttendanceData(
                            listId = attendanceId,
                            body = EditAttendanceBody(status = status)
                        )
                    } else {
                        attendanceRepository.deleteAttendanceData(listOf(att.id))
                    }
                }

            }

            PermitType.ABSENCE -> attendanceRepository.deleteAttendanceData(attendanceId)
        }
    }

    private fun getDispensationStatus(time: LocalTime?): AttendanceStatus {
        time ?: throw CommonException(Message.INVALID_TIME_FORMAT)
        val offsetTime = OffsetTime.of(time, Utils.getOffset())
        return getAttendanceStatus(offsetTime.toLocalTime())
    }


    private fun getAttendanceStatus(
        checkInTime: LocalTime,
    ): AttendanceStatus {
        println(checkInTime)

        val setting = generalSetting.get()

        val status = when (checkInTime) {
            in setting.checkInPeriodStart..setting.schoolPeriodStart -> AttendanceStatus.CHECKED_IN
            in setting.schoolPeriodStart..setting.checkInPeriodEnd -> AttendanceStatus.LATE
            else -> throw CommonException(Message.OUTSIDE_TIME_PERIOD)
        }

        return status
    }

    private fun validateAttendanceStatus(studentId: String): Boolean {
        val allowedStatus = listOf(AttendanceStatus.CHECKED_IN, AttendanceStatus.LATE)
        val currentDate = OffsetDateTime.now(Utils.getOffset()).toInstant().toEpochMilli()
        val queryParams =
            GetAttendanceByStudentQueryParams(dateRange = listOf(currentDate, currentDate))

        val attendanceData = attendanceRepository.getAttendanceListByStudent(
            studentId = studentId,
            queryParams = queryParams
        ).lastOrNull()

        return attendanceData?.status in allowedStatus
    }

    private suspend fun handleEditWithAttachment(
        id: String,
        body: EditPermitBody,
        attachment: File,
    ) {

        val path = Utils.getUploadDir(Constant.PERMIT_FILE_PATH, attachment.name)
        val upload = fileService.uploadFile(
            filePath = path,
            file = attachment
        )
        transactionManager.suspendTransaction {
            val permit = permitRepository.getPermitById(id)
                ?: throw "permit".notFoundException()

            if (permit.approvalStatus != ApprovalStatus.APPROVAL_PENDING)
                throw "permit".illegalStatusExeptions()

            permitRepository.editPermit(listOf(id), body.copy(attachment = upload))
        }


    }

    private suspend fun handleEditWithoutAttachment(
        id: String,
        body: EditPermitBody,
    ) = transactionManager.suspendTransaction {
        val permit = permitRepository.getPermitById(id)
            ?: throw "permit".notFoundException()

        if (permit.approvalStatus != ApprovalStatus.APPROVAL_PENDING)
            throw "permit".illegalStatusExeptions()

        permitRepository.editPermit(
            listId = listOf(id),
            body = body
        )
    }

}