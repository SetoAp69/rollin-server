package com.rollinup.server.service.permit

import com.rollinup.server.CommonException
import com.rollinup.server.Constant
import com.rollinup.server.datasource.api.model.ApiResponse
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.permit.PermitListEntity
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.PermitMapper
import com.rollinup.server.model.ApprovalStatus
import com.rollinup.server.model.PermitType
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
import com.rollinup.server.util.Utils.toLocalDateTime
import com.rollinup.server.util.deleteFileException
import com.rollinup.server.util.generalsetting.GeneralSettingCache
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
                listId = body.listId
            ),
        )

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
        val path = Utils.getUploadDir(path = Constant.PERMIT_FILE_PATH)
        val file = fileHash.get("attachment")
            ?: throw "permit attachment".uploadFileException()

        val upload = fileService.uploadFile(path, file)

        if (upload !is ApiResponse.Success)
            throw "permit attachment".uploadFileException()

        try {
            transactionManager.suspendTransaction {
                if (body.type == PermitType.DISPENSATION && !validateAttendanceStatus(body.studentId))
                    throw "attendance".illegalStatusExeptions()

                permitRepository.createPermit(body.copy(attachment = upload.data))
            }

        } catch (e: Exception) {
            fileService.deleteFile(upload.data)
            throw e
        } finally {
            fileHash.forEach { _, file -> file.delete() }
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
            message = "permit".successEditResponse() ,
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

        val delete = fileService.deleteFile(permit.map { it.attachment })

        when (delete) {
            is ApiResponse.Error -> throw "permit".deleteFileException()

            is ApiResponse.Success -> transactionManager.suspendTransaction {
                val attendanceList = attendanceRepository.getAttendanceListByPermit(id)
                val attendanceListByPermit = attendanceList.groupBy { it.permit }

                permitRepository.deletePermit(id)
                attendanceListByPermit.forEach { permit, att ->
                    permit ?: return@forEach

                    rollBackAttendance(
                        attendanceList = att,
                        permitType = permit.type,
                    )
                }
            }
        }

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

        val attendanceByPermit = attendanceList.groupBy { it.permit }

        attendanceByPermit.forEach { permit, att ->
            permit ?: return@forEach
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
                val status = getDispensationStatus(
                    time = attendanceList.firstOrNull()?.checkedInAt?.toLocalDateTime()
                        ?.toLocalTime()
                )

                attendanceRepository.updateAttendanceData(
                    listId = attendanceId,
                    body = EditAttendanceBody(status = status)
                )
            }

            PermitType.ABSENCE -> attendanceRepository.deleteAttendanceData(attendanceId)
        }
    }

    private fun getDispensationStatus(time: LocalTime?): AttendanceStatus {
        time ?: throw CommonException(Message.INVALID_TIME_FORMAT)
        val offsetTime = OffsetTime.of(time, Utils.getOffset())
        return getAttendanceStatus(offsetTime)
    }


    private fun getAttendanceStatus(
        checkInTime: OffsetTime,
    ): AttendanceStatus {

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
        val path = Utils.getUploadDir(Constant.PERMIT_FILE_PATH)
        val upload = fileService.uploadFile(path, attachment)

        if (upload !is ApiResponse.Success)
            throw "permit attachment".uploadFileException()

        try {
            var oldAttachment = ""

            transactionManager.suspendTransaction {
                val permit = permitRepository.getPermitById(id)
                    ?: throw "permit".notFoundException()

                if (permit.approvalStatus != ApprovalStatus.APPROVAL_PENDING)
                    throw "permit".illegalStatusExeptions()

                oldAttachment = permit.attachment

                permitRepository.editPermit(listOf(id), body.copy(attachment = upload.data))
            }

            val delete = fileService.deleteFile(oldAttachment)

            if (delete !is ApiResponse.Success)
                throw "permit attachment".uploadFileException()

        } catch (e: Exception) {
            fileService.deleteFile(upload.data)
            throw e
        } finally {
            attachment.delete()
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