package com.rollinup.server.service.permit

import com.rollinup.server.CommonException
import com.rollinup.server.MockkEnvironment
import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.holiday.HolidayCache
import com.rollinup.server.datasource.database.model.ApprovalStatus
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.PermitType
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import com.rollinup.server.datasource.database.model.permit.PermitByIdEntity
import com.rollinup.server.datasource.database.model.permit.PermitListEntity
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.PermitMapper
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import com.rollinup.server.model.request.permit.CreatePermitBody
import com.rollinup.server.model.request.permit.EditPermitBody
import com.rollinup.server.model.request.permit.GetPermitQueryParams
import com.rollinup.server.model.request.permit.PermitApprovalBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.permit.GetPermitByIdResponse
import com.rollinup.server.model.response.permit.GetPermitListByClassResponse
import com.rollinup.server.model.response.permit.GetPermitListByStudentResponse
import com.rollinup.server.service.file.FileService
import com.rollinup.server.util.Utils
import com.rollinup.server.util.manager.TransactionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.Transaction
import org.junit.After
import org.junit.Before
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class PermitServiceImplTest {
    private lateinit var permitService: PermitServiceImpl

    private var permitMapper = PermitMapper()

    private val envMock = MockkEnvironment()

    @MockK
    private lateinit var permitRepository: PermitRepository

    @MockK
    private lateinit var attendanceRepository: AttendanceRepository

    @MockK
    private lateinit var fileService: FileService

    @MockK
    private lateinit var generalSetting: GeneralSettingCache

    @MockK
    private lateinit var holidayCache: HolidayCache

    @MockK
    private lateinit var transactionManager: TransactionManager

    private var file = mockk<File>()

    private val generalSettingMock = GeneralSetting(
        semesterStart = OffsetDateTime.now(Utils.getOffset()).minusMonths(3),
        semesterEnd = OffsetDateTime.now(Utils.getOffset()).minusMonths(3),
        checkInPeriodStart = LocalTime.of(6, 30, 0, 0),
        checkInPeriodEnd = LocalTime.of(7, 15, 0, 0),
        schoolPeriodStart = LocalTime.of(7, 0, 0, 0),
        schoolPeriodEnd = LocalTime.of(15, 0, 0, 0),
        rad = 0.6,
        long = 123.123,
        lat = 12.2222,
        updatedAt = OffsetDateTime.now(Utils.getOffset()),
        modifiedBy = "123",
        modifiedByName = "modifiedBy"
    )

    private var validDateTimeInstant = Instant.parse("2025-10-28T09:00:00+07:00")

    private fun arrangeMockkNow(time: OffsetDateTime) {
        mockkStatic(OffsetDateTime::class)

        coEvery {
            OffsetDateTime.now(any<ZoneOffset>())
        } returns time
    }

    private fun arrangeSuspendTransaction() {
        coEvery {
            transactionManager.suspendTransaction<Any>(any())
        } answers {
            val block = args.first() as Function1<Transaction, Any>
            val mockedTransaction = mockk<Transaction>(relaxed = true)

            block.invoke(mockedTransaction)
        }
    }

    private fun arrangeHoliday(
        data: List<LocalDate>,
    ) {
        coEvery {
            holidayCache.get()
        } returns data
    }

    private fun arrangeGeneralSetting(
        data: GeneralSetting,
    ) {
        coEvery {
            generalSetting.get()
        } returns data
    }


    @Before
    fun setUp() {
        envMock.setup()
        MockKAnnotations.init(this)
        permitService = PermitServiceImpl(
            permitRepository = permitRepository,
            attendanceRepository = attendanceRepository,
            permitMapper = permitMapper,
            transactionManager = transactionManager,
            fileService = fileService,
            generalSetting = generalSetting,
            holidayCache = holidayCache
        )

        coEvery { file.name } returns "file-name.jpg"
        arrangeSuspendTransaction()
        arrangeHoliday(emptyList())
        arrangeGeneralSetting(generalSettingMock)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    //getPermitByStudent Test

    @Test
    fun `getPermitByStudent should return correct response data`() = runTest {
        //Arrange
        val queryParams = GetPermitQueryParams(
            limit = 100,
            page = 2,
        )

        val studentId = "id"

        val expectedList = listOf(
            PermitListEntity(
                id = "id",
                name = "name",
                date = "2025-10-04",
                student = PermitListEntity.User(
                    id = "studentId",
                    name = "studentName",
                    username = "userName",
                    classX = "class"
                ),
                approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                type = PermitType.DISPENSATION,
                attachment = "attachment",
                reason = "no reason at all",
                permitStart = "2025-10-04T04:16:25.123456Z",
                permitEnd = "2025-10-04T04:16:25.123456Z",
                createdAt = "2025-10-04T04:16:25.123456Z",
                updatedAt = "2025-10-04T04:16:25.123456Z"
            ),
            PermitListEntity(
                id = "id2",
                name = "name2",
                date = "2025-10-04",
                student = PermitListEntity.User(
                    id = "studentId2",
                    name = "studentName2",
                    username = "userName2",
                    classX = "class"
                ),
                approvalStatus = ApprovalStatus.APPROVED,
                type = PermitType.ABSENCE,
                attachment = "attachment",
                reason = "yes",
                permitStart = "2025-10-04T04:16:25.123456Z",
                permitEnd = "2025-10-04T04:16:25.123456Z",
                createdAt = "2025-10-04T04:16:25.123456Z",
                updatedAt = "2025-10-04T04:16:25.123456Z"
            )
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting permit data",
            data = GetPermitListByStudentResponse(
                record = 2,
                page = 2,
                data = listOf(
                    GetPermitListByStudentResponse.PermitListDTO(
                        id = "id",
                        studentId = "studentId",
                        name = "name",
                        date = "2025-10-04",
                        startTime = "2025-10-04T04:16:25.123456Z",
                        reason = "no reason at all",
                        approvalStatus = ApprovalStatus.APPROVAL_PENDING.value,
                        type = PermitType.DISPENSATION.value,
                        endTime = "2025-10-04T04:16:25.123456Z",
                        createdAt = "2025-10-04T04:16:25.123456Z"
                    ),
                    GetPermitListByStudentResponse.PermitListDTO(
                        id = "id2",
                        studentId = "studentId2",
                        name = "name2",
                        date = "2025-10-04",
                        startTime = "2025-10-04T04:16:25.123456Z",
                        reason = "yes",
                        approvalStatus = ApprovalStatus.APPROVED.value,
                        type = PermitType.ABSENCE.value,
                        endTime = "2025-10-04T04:16:25.123456Z",
                        createdAt = "2025-10-04T04:16:25.123456Z"
                    )
                )
            )
        )


        coEvery {
            permitRepository.getPermitList(queryParams = queryParams, studentId = studentId)
        } returns expectedList

        //Act
        val response = permitService.getPermitByStudent(studentId, queryParams)

        //Assert
        coVerify {
            permitRepository.getPermitList(queryParams = queryParams, studentId = studentId)
        }

        assertEquals(expectedResponse, response)
        assertEquals(200, response.status)
        assert(response.data!!.data.isNotEmpty())
    }

    //getPermitByClass Test
    @Test
    fun `getPermitByClass should return correct response data`() = runTest {
        //Arrange
        val queryParams = GetPermitQueryParams(
            limit = 10,
            page = 1,
        )
        val classKey = 69

        val expectedList = listOf(
            PermitListEntity(
                id = "id",
                name = "name",
                date = "2025-10-04",
                student = PermitListEntity.User(
                    id = "studentId",
                    name = "studentName",
                    username = "userName",
                    classX = "class"
                ),
                approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                type = PermitType.DISPENSATION,
                attachment = "attachment",
                reason = "no reason at all",
                permitStart = "2025-10-04T04:16:25.123456Z",
                permitEnd = "2025-10-04T04:16:25.123456Z",
                createdAt = "2025-10-04T04:16:25.123456Z",
                updatedAt = "2025-10-04T04:16:25.123456Z"
            ),
            PermitListEntity(
                id = "id2",
                name = "name2",
                date = "2025-10-04",
                student = PermitListEntity.User(
                    id = "studentId2",
                    name = "studentName2",
                    username = "userName2",
                    classX = "class"
                ),
                approvalStatus = ApprovalStatus.APPROVED,
                type = PermitType.ABSENCE,
                attachment = "attachment",
                reason = "yes",
                permitStart = "2025-10-04T04:16:25.123456Z",
                permitEnd = "2025-10-04T04:16:25.123456Z",
                createdAt = "2025-10-04T04:16:25.123456Z",
                updatedAt = "2025-10-04T04:16:25.123456Z"
            )
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting permit data",
            data = GetPermitListByClassResponse(
                record = 2,
                page = 1,
                data = listOf(
                    GetPermitListByClassResponse.PermitListDTO(
                        id = "id",
                        name = "name",
                        date = "2025-10-04",
                        startTime = "2025-10-04T04:16:25.123456Z",
                        reason = "no reason at all",
                        approvalStatus = ApprovalStatus.APPROVAL_PENDING.value,
                        type = PermitType.DISPENSATION.value,
                        endTime = "2025-10-04T04:16:25.123456Z",
                        student = GetPermitListByClassResponse.User(
                            id = "studentId",
                            name = "studentName",
                            xClass = "class"
                        ),
                        createdAt = "2025-10-04T04:16:25.123456Z"
                    ),
                    GetPermitListByClassResponse.PermitListDTO(
                        id = "id2",
                        name = "name2",
                        date = "2025-10-04",
                        startTime = "2025-10-04T04:16:25.123456Z",
                        reason = "yes",
                        approvalStatus = ApprovalStatus.APPROVED.value,
                        type = PermitType.ABSENCE.value,
                        endTime = "2025-10-04T04:16:25.123456Z",
                        student = GetPermitListByClassResponse.User(
                            id = "studentId2",
                            name = "studentName2",
                            xClass = "class"
                        ),
                        createdAt = "2025-10-04T04:16:25.123456Z"
                    )
                )
            )
        )

        coEvery {
            permitRepository.getPermitList(
                queryParams = queryParams,
                classKey = classKey
            )
        } returns expectedList

        //ACT
        val result = permitService.getPermitByClass(classKey = classKey, queryParams = queryParams)

        //Assert
        coVerify {
            permitRepository.getPermitList(
                queryParams = queryParams,
                classKey = classKey
            )
        }

        assertEquals(200, result.status)
        assertEquals(expectedResponse, result)
    }

    //getPermitById test
    @Test
    fun `getPermitById should return correct permit data`() = runTest {
        //Arrange
        val id = "id"
        val expectedEntity = PermitByIdEntity(
            id = "permitId",
            name = "name",
            student = PermitByIdEntity.User(
                id = "userId",
                name = "studentName",
                studentId = "studentId",
                username = "userName",
                classX = "class"
            ),
            date = "2025-10-04T04:16:25.123456Z",
            approvalStatus = ApprovalStatus.APPROVED,
            type = PermitType.ABSENCE,
            reason = "sick",
            note = "note",
            permitStart = "2025-10-04T04:16:25.123456Z",
            permitEnd = "2025-10-04T04:16:25.123456Z",
            approvedBy = PermitByIdEntity.User(
                id = "approverId",
                name = "approverName",
                username = "apUserName",
                classX = null
            ),
            approvalNote = "approval note",
            createdAt = "2025-10-04T04:16:25.123456Z",
            updatedAt = "2025-10-04T04:16:25.123456Z",
            attachment = "attachment",
            approvedAt = "2025-10-04T04:16:25.123456Z"
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting permit data",
            data = GetPermitByIdResponse(
                id = "permitId",
                date = "2025-10-04T04:16:25.123456Z",
                name = "name",
                student = GetPermitByIdResponse.User(
                    id = "userId",
                    name = "studentName",
                    username = "userName",
                    studentId = "studentId",
                    xClass = "class"
                ),
                attachment = "attachment",
                startTime = "2025-10-04T04:16:25.123456Z",
                endTime = "2025-10-04T04:16:25.123456Z",
                note = "note",
                reason = "sick",
                createdAt = "2025-10-04T04:16:25.123456Z",
                updatedAt = "2025-10-04T04:16:25.123456Z",
                approvalStatus = ApprovalStatus.APPROVED.value,
                approvalNote = "approval note",
                approvedBy = GetPermitByIdResponse.User(
                    id = "approverId",
                    name = "approverName",
                    username = "apUserName",
                    xClass = null
                ),
                approvedAt = "2025-10-04T04:16:25.123456Z"
            )
        )


        coEvery {
            permitRepository.getPermitById(id)
        } returns expectedEntity

        //Act
        val result = permitService.getPermitById(id)

        //Assert
        coVerify {
            permitRepository.getPermitById(id)
        }

        assertEquals(expectedResponse, result)
        assertEquals(200, result.status)
        assert(result.data != null)
    }

    @Test
    fun `getPermitById should return throw correct exceptions when data not found`() = runTest {
        //Arrange
        val id = "id"
        val expectedMessage = "can't find permit data"

        coEvery {
            permitRepository.getPermitById(id)
        } returns null

        //Act
        val result = assertFailsWith<CommonException> { permitService.getPermitById(id) }

        //Assert
        coVerify {
            permitRepository.getPermitById(id)
        }

        assertEquals(expectedMessage, result.message)
    }

    //region doApproval test
    @Test
    fun `doApproval should changes permit data and connected attendance status to excused when permit type is dispensation and return correct response when approved`() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId: String = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "approvalNote",
                isApproved = true
            )
            val editPermitBody = EditPermitBody(
                approvedBy = approverId,
                approvalStatus = ApprovalStatus.APPROVED,
                approvalNote = body.approvalNote
            )

            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION,
                )
            )
            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.DISPENSATION
                    ),
                    student = AttendanceEntity.User(
                        id = "userId"
                    )
                )
            )

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "permit data successfully updated",
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery {
                attendanceRepository.updateAttendanceData(
                    listId = listOf("attendanceId"),
                    body = EditAttendanceBody(status = AttendanceStatus.EXCUSED)
                )
            } just runs

            coEvery {
                permitRepository.editPermit(listOf("permitId"), editPermitBody)
            } just runs

            //Act
            val result = permitService.doApproval(approverId, body)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { permitRepository.editPermit(listOf("permitId"), editPermitBody) }
            coVerify {
                attendanceRepository.updateAttendanceData(
                    listOf("attendanceId"),
                    EditAttendanceBody(status = AttendanceStatus.EXCUSED)
                )
            }
        }


    @Test
    fun `doApproval should changes permit data and connected attendance status to absent when permit type is absence and return correct response when approved`() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "approvalNote",
                isApproved = true
            )
            val editPermitBody = EditPermitBody(
                approvedBy = approverId,
                approvalStatus = ApprovalStatus.APPROVED,
                approvalNote = body.approvalNote
            )

            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.ABSENCE
                    ),
                    student = AttendanceEntity.User(
                        id = "userId"
                    )
                )
            )

            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION,
                )
            )

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "permit data successfully updated",
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery {
                attendanceRepository.updateAttendanceData(
                    listId = listOf("attendanceId"),
                    body = EditAttendanceBody(status = AttendanceStatus.ABSENT)
                )
            } just runs

            coEvery {
                permitRepository.editPermit(listOf("permitId"), editPermitBody)
            } just runs

            //Act
            val result = permitService.doApproval(approverId, body)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { permitRepository.editPermit(listOf("permitId"), editPermitBody) }
            coVerify {
                attendanceRepository.updateAttendanceData(
                    listOf("attendanceId"),
                    EditAttendanceBody(status = AttendanceStatus.ABSENT)
                )
            }
        }

    @Test
    fun `doApproval should changes permit data and delete connected attendance data when is declined and permit type is absence `() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId: String = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "approvalNote",
                isApproved = false
            )
            val editPermitBody = EditPermitBody(
                approvedBy = approverId,
                approvalStatus = ApprovalStatus.DECLINED,
                approvalNote = body.approvalNote
            )

            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.ABSENCE,
                )
            )
            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.ABSENCE
                    ),
                    student = AttendanceEntity.User(
                        id = "userId"
                    )
                )
            )

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "permit data successfully updated",
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery { attendanceRepository.deleteAttendanceData(listOf("attendanceId")) } just runs

            coEvery {
                permitRepository.editPermit(listOf("permitId"), editPermitBody)
            } just runs

            //Act
            val result = permitService.doApproval(approverId, body)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { permitRepository.editPermit(listOf("permitId"), editPermitBody) }
            coVerify { attendanceRepository.deleteAttendanceData(listOf("attendanceId")) }
        }

    @Test
    fun `doApproval should changes permit data and delete connected attendance data when is declined and permit type is dispensation and checkInTime was null `() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId: String = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "approvalNote",
                isApproved = false
            )
            val editPermitBody = EditPermitBody(
                approvedBy = approverId,
                approvalStatus = ApprovalStatus.DECLINED,
                approvalNote = body.approvalNote
            )

            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.ABSENCE
                    ),
                    student = AttendanceEntity.User(
                        id = "userId"
                    ),
                    checkedInAt = null
                )
            )
            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION,
                )
            )

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "permit data successfully updated",
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery { attendanceRepository.deleteAttendanceData(listOf("attendanceId")) } just runs

            coEvery {
                permitRepository.editPermit(listOf("permitId"), editPermitBody)
            } just runs

            //Act
            val result = permitService.doApproval(approverId, body)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { permitRepository.editPermit(listOf("permitId"), editPermitBody) }
            coVerify { attendanceRepository.deleteAttendanceData(listOf("attendanceId")) }
        }

    @Test
    fun `doApproval should changes permit data and connected attendance status to checked_in when is declined and permit type is dispensation and check in time is before school period start`() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId: String = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "approvalNote",
                isApproved = false
            )
            val editPermitBody = EditPermitBody(
                approvedBy = approverId,
                approvalStatus = ApprovalStatus.DECLINED,
                approvalNote = body.approvalNote
            )

            val checkInTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()

            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION,
                )
            )

            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.DISPENSATION
                    ),
                    student = AttendanceEntity.User(
                        id = "userId"
                    ),
                    checkedInAt = checkInTime
                )
            )

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "permit data successfully updated",
            )

            val localTimeNowMockk = LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset())

            val editAttendanceBody = EditAttendanceBody(
                status = AttendanceStatus.CHECKED_IN
            )

            arrangeGeneralSetting(
                generalSettingMock.copy(
                    checkInPeriodStart = localTimeNowMockk.minusMinutes(20),
                    checkInPeriodEnd = localTimeNowMockk.plusMinutes(25),
                    schoolPeriodStart = localTimeNowMockk.plusMinutes(15)
                )
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery {
                attendanceRepository.updateAttendanceData(
                    listOf("attendanceId"),
                    editAttendanceBody
                )
            } just runs

            coEvery {
                permitRepository.editPermit(listOf("permitId"), editPermitBody)
            } just runs

            //Act
            val result = permitService.doApproval(approverId, body)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { permitRepository.editPermit(listOf("permitId"), editPermitBody) }
            coVerify {
                attendanceRepository.updateAttendanceData(
                    listOf("attendanceId"),
                    editAttendanceBody
                )
            }
        }

    @Test
    fun `doApproval should changes permit data and connected attendance data to late when is declined and permit type is dispensation and check in time is after school period start`() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId: String = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "approvalNote",
                isApproved = false
            )
            val editPermitBody = EditPermitBody(
                approvedBy = approverId,
                approvalStatus = ApprovalStatus.DECLINED,
                approvalNote = body.approvalNote
            )

            val checkInTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()
            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION,
                )
            )
            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.DISPENSATION
                    ),
                    student = AttendanceEntity.User(
                        id = "userId"
                    ),
                    checkedInAt = checkInTime
                )
            )

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "permit data successfully updated",
            )

            val localTimeNowMockk = LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset())

            val editAttendanceBody = EditAttendanceBody(
                status = AttendanceStatus.LATE
            )

            arrangeGeneralSetting(
                generalSettingMock.copy(
                    checkInPeriodStart = localTimeNowMockk.minusMinutes(20),
                    checkInPeriodEnd = localTimeNowMockk.plusMinutes(25),
                    schoolPeriodStart = localTimeNowMockk.minusMinutes(5)
                )
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery {
                attendanceRepository.updateAttendanceData(
                    listOf("attendanceId"),
                    editAttendanceBody
                )
            } just runs

            coEvery {
                permitRepository.editPermit(listOf("permitId"), editPermitBody)
            } just runs

            //Act
            val result = permitService.doApproval(approverId, body)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { permitRepository.editPermit(listOf("permitId"), editPermitBody) }
            coVerify {
                attendanceRepository.updateAttendanceData(
                    listOf("attendanceId"),
                    editAttendanceBody
                )
            }
        }

    @Test
    fun `doApproval should throw exceptions when permit list with given id is empty`() = runTest {
        //Arrange
        val approverId = "approverId"
        val body = PermitApprovalBody(
            listId = listOf("permitId"),
            isApproved = true
        )
        val editPermitBody = EditPermitBody(
            approvalStatus = ApprovalStatus.APPROVED,
            approvalNote = ""
        )
        val queryParams = GetPermitQueryParams(
            listId = listOf("permitId"),
            isActive = true
        )

        val expectedMessage = "can't find permit data"

        coEvery {
            permitRepository.getPermitList(queryParams = queryParams)
        } returns emptyList()

        //Act
        val result = assertFailsWith<CommonException> {
            permitService.doApproval(approverId, body)
        }

        //Assert
        coVerify {
            permitRepository.getPermitList(queryParams)
        }

        assertEquals(expectedMessage, result.message)
    }

    //endregion

    //region getPermit

    @Test
    fun `createPermit should return correct response when success`() = runTest {
        //Arrange
        val permitId = "permitId"
        val validLongDateTime = validDateTimeInstant.toEpochMilli()
        val validOffsetDateTime = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset())
        val validCurrentDate =
            LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString().replace("-", "")
        val uploadedFilePath = "UPLOAD_DIR/attachment/permit/$validCurrentDate/file-name.jpg"

        val studentId = "studentId"
        val reason = "reason"
        val duration = "[$validLongDateTime, $validLongDateTime]"
        val type = PermitType.ABSENCE


        val formHashMap = hashMapOf(
            "studentId" to studentId,
            "reason" to reason,
            "duration" to duration,
            "type" to type.value
        )

        val fileHashMap = hashMapOf(
            "attachment" to file
        )

        val body = CreatePermitBody(
            studentId = studentId,
            reason = reason,
            duration = listOf(validLongDateTime, validLongDateTime),
            type = type,
            attachment = uploadedFilePath
        )

        val expectedResponse = Response<Unit>(
            status = 201,
            message = "permit data successfully created",
        )

        arrangeMockkNow(validOffsetDateTime)

        coEvery {
            fileService.uploadFile(uploadedFilePath, file)
        } returns uploadedFilePath

        coEvery {
            permitRepository.createPermit(body)
        } returns permitId

        coEvery {
            attendanceRepository.createAttendanceFromPermit(
                permitId,
                studentId,
                any(),
                AttendanceStatus.APPROVAL_PENDING
            )
        } just runs

        //Act
        val result = permitService.createPermit(formHashMap, fileHashMap)

        //Assert
        assertEquals(expectedResponse, result)
        assertEquals(201, result.status)
        coVerify {
            attendanceRepository.createAttendanceFromPermit(
                permitId,
                studentId,
                any(),
                AttendanceStatus.APPROVAL_PENDING
            )
            permitRepository.createPermit(body)
        }
    }


    @Test
    fun `createPermit should throw exceptions when formHashMap is empty`() = runTest {
        //Arrange
        val formHashMap = hashMapOf<String, String>()
        val fileHashMap = hashMapOf<String, File>(
            "attachment" to file
        )

        //Act & Assert
        val result = assertFailsWith<IllegalArgumentException> {
            permitService.createPermit(formHashMap, fileHashMap)
        }
    }


    @Test
    fun `createPermit should throw exceptions when fileHashMap is empty`() = runTest {
        //Arrange
        val formHashMap = hashMapOf<String, String>(
            "studentId" to "studentId",
            "reason" to "reason",
            "type" to "type"
        )
        val fileHashMap = hashMapOf<String, File>()

        //Act & Assert
        assertFailsWith<IllegalArgumentException> {
            permitService.createPermit(formHashMap, fileHashMap)
        }
    }


    @Test
    fun `createPermit should throw exceptions when attachment is null`() = runTest {
        //Arrange
        val formHashMap = hashMapOf<String, String>(
            "studentId" to "studentId",
            "reason" to "reason",
            "type" to "type",
            "duration" to "[123,123]",
        )
        val fileHashMap = hashMapOf<String, File>(
            "anything but attachment" to file
        )

        val expectedMessage = "failed to upload permit attachment file"

        //Act & Assert
        val result = assertFailsWith<CommonException> {
            permitService.createPermit(formHashMap, fileHashMap)
        }

        assertEquals(expectedMessage, result.message)
    }

    @Test
    fun `createPermit should throw exceptions if all included dates are holidays`() =
        runTest {
            //Arrange
            val permitId = "permitId"
            val validLongDateTime = validDateTimeInstant.toEpochMilli()
            val validOffsetDateTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset())
            val validCurrentDate =
                LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()
                    .replace("-", "")
            val uploadedFilePath = "UPLOAD_DIR/attachment/permit/$validCurrentDate/file-name.jpg"

            val studentId = "studentId"
            val reason = "reason"
            val duration = "[$validLongDateTime, $validLongDateTime]"
            val type = PermitType.ABSENCE


            val formHashMap = hashMapOf(
                "studentId" to studentId,
                "reason" to reason,
                "duration" to duration,
                "type" to type.value
            )

            val fileHashMap = hashMapOf(
                "attachment" to file
            )

            val body = CreatePermitBody(
                studentId = studentId,
                reason = reason,
                duration = listOf(validLongDateTime, validLongDateTime),
                type = type,
                attachment = uploadedFilePath
            )

            val expectedMessage = "Invalid durations"

            arrangeMockkNow(validOffsetDateTime)
            arrangeHoliday(
                listOf(
                    LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset())
                )
            )

            coEvery {
                fileService.uploadFile(uploadedFilePath, file)
            } returns uploadedFilePath

            //Act & Assert
            val result = assertFailsWith<CommonException> {
                permitService.createPermit(
                    formHashMap,
                    fileHashMap
                )
            }

            //Assert
            assertEquals(expectedMessage, result.message)
        }

    @Test
    fun `createPermit should throw exceptions if all included dates are weekends`() =
        runTest {
            //Arrange
            val weekendInstant = OffsetDateTime.parse("2025-11-02T09:00:00+07:00").toInstant()
            val validLongDateTime = weekendInstant.toEpochMilli()
            val validOffsetDateTime =
                OffsetDateTime.ofInstant(weekendInstant, Utils.getOffset())
            val validCurrentDate =
                LocalDate.ofInstant(weekendInstant, Utils.getOffset()).toString()
                    .replace("-", "")
            val uploadedFilePath = "UPLOAD_DIR/attachment/permit/$validCurrentDate/file-name.jpg"

            val studentId = "studentId"
            val reason = "reason"
            val duration = "[$validLongDateTime, $validLongDateTime]"
            val type = PermitType.ABSENCE


            val formHashMap = hashMapOf(
                "studentId" to studentId,
                "reason" to reason,
                "duration" to duration,
                "type" to type.value
            )

            val fileHashMap = hashMapOf(
                "attachment" to file
            )

            val expectedMessage = "Invalid durations"

            arrangeMockkNow(validOffsetDateTime)

            coEvery {
                fileService.uploadFile(uploadedFilePath, file)
            } returns uploadedFilePath

            //Act & Assert
            val result = assertFailsWith<CommonException> {
                permitService.createPermit(
                    formHashMap,
                    fileHashMap
                )
            }

            //Assert
            assertEquals(expectedMessage, result.message)
        }

    //endregion

    //region editPermit test

    @Test
    fun `editPermit() with attachment should return correct response`() = runTest {
        //Arrange
        val permitId = "permitId"
        val formHashMap = hashMapOf<String, String>(
            "reason" to "idk"
        )

        val fileHashMap = hashMapOf<String, File>(
            "attachment" to file
        )
        val validCurrentDate =
            LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString().replace("-", "")
        val validCurrentDateTime = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset())
        val uploadedFilePath = "UPLOAD_DIR/attachment/permit/$validCurrentDate/file-name.jpg"

        val body = EditPermitBody(
            reason = "idk",
            attachment = uploadedFilePath
        )

        val mockEntity = PermitByIdEntity(
            approvalStatus = ApprovalStatus.APPROVAL_PENDING
        )

        val expectedResponse = Response<Unit>(
            status = 202,
            message = "permit data successfully updated",
        )

        arrangeMockkNow(validCurrentDateTime)

        coEvery {
            fileService.uploadFile(uploadedFilePath, file)
        } returns uploadedFilePath

        coEvery {
            permitRepository.getPermitById(permitId)
        } returns mockEntity

        coEvery {
            permitRepository.editPermit(listOf(permitId), body)
        } just runs


        //Act
        val result = permitService.editPermit(permitId, formHashMap, fileHashMap)

        //Assert
        assertEquals(expectedResponse, result)
        assertEquals(202, result.status)
        assertEquals("permit data successfully updated", result.message)
        coVerify {
            fileService.uploadFile(uploadedFilePath, file)
            permitRepository.editPermit(listOf(permitId), body)
        }
    }

    @Test
    fun `editPermit() without attachment should return correct response`() = runTest {
        //Arrange
        val permitId = "permitId"
        val formHashMap = hashMapOf<String, String>(
            "reason" to "idk"
        )
        val fileHashMap = hashMapOf<String, File>()
        val body = EditPermitBody(
            reason = "idk",
        )
        val mockEntity = PermitByIdEntity(
            approvalStatus = ApprovalStatus.APPROVAL_PENDING
        )
        val expectedResponse = Response<Unit>(
            status = 202,
            message = "permit data successfully updated",
        )

        coEvery {
            permitRepository.getPermitById(permitId)
        } returns mockEntity

        coEvery {
            permitRepository.editPermit(listOf(permitId), body)
        } just runs


        //Act
        val result = permitService.editPermit(permitId, formHashMap, fileHashMap)

        //Assert
        assertEquals(expectedResponse, result)
        assertEquals(202, result.status)
        assertEquals("permit data successfully updated", result.message)
        coVerify {
            permitRepository.editPermit(listOf(permitId), body)
        }
    }

    @Test
    fun `editPermit() with attachment should throw correct exceptions when permit is not found`() =
        runTest {
            //Arrange
            val permitId = "permitId"
            val formHashMap = hashMapOf<String, String>(
                "reason" to "idk"
            )

            val fileHashMap = hashMapOf<String, File>(
                "attachment" to file
            )
            val validCurrentDate =
                LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()
                    .replace("-", "")
            val validCurrentDateTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset())
            val uploadedFilePath = "UPLOAD_DIR/attachment/permit/$validCurrentDate/file-name.jpg"

            val body = EditPermitBody(
                reason = "idk",
                attachment = uploadedFilePath
            )

            val expectedMessage = "can't find permit data"

            arrangeMockkNow(validCurrentDateTime)

            coEvery {
                fileService.uploadFile(uploadedFilePath, file)
            } returns uploadedFilePath

            coEvery {
                permitRepository.getPermitById(permitId)
            } returns null

            //Act
            val result = assertFailsWith<CommonException> {
                permitService.editPermit(
                    permitId,
                    formHashMap,
                    fileHashMap
                )
            }

            //Assert
            assertEquals(expectedMessage, result.message)
            coVerify {
                fileService.uploadFile(uploadedFilePath, file)
            }
            coVerify(exactly = 0) {
                permitRepository.editPermit(listOf(permitId), body)
            }
        }

    @Test
    fun `editPermit() without attachment should throw correct exceptions when permit is not found`() =
        runTest {
            //Arrange
            val permitId = "permitId"
            val formHashMap = hashMapOf<String, String>(
                "reason" to "idk"
            )
            val fileHashMap = hashMapOf<String, File>()
            val body = EditPermitBody(
                reason = "idk",
            )
            val expectedMessage = "can't find permit data"

            coEvery {
                permitRepository.getPermitById(permitId)
            } returns null


            //Act
            val result = assertFailsWith<CommonException> {
                permitService.editPermit(
                    permitId,
                    formHashMap,
                    fileHashMap
                )
            }

            //Assert
            assertEquals(expectedMessage, result.message)
            coVerify(exactly = 0) {
                permitRepository.editPermit(listOf(permitId), body)
            }
        }

    @Test
    fun `editPermit() with attachment should throw correct exceptions when permit is not in pending status`() =
        runTest {
            //Arrange
            val permitId = "permitId"
            val formHashMap = hashMapOf<String, String>(
                "reason" to "idk"
            )

            val fileHashMap = hashMapOf<String, File>(
                "attachment" to file
            )
            val validCurrentDate =
                LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()
                    .replace("-", "")
            val validCurrentDateTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset())
            val uploadedFilePath = "UPLOAD_DIR/attachment/permit/$validCurrentDate/file-name.jpg"

            val body = EditPermitBody(
                reason = "idk",
                attachment = uploadedFilePath
            )

            val mockEntity = PermitByIdEntity(
                approvalStatus = ApprovalStatus.APPROVED
            )
            val expectedMessage = "Illegal permit status"

            arrangeMockkNow(validCurrentDateTime)

            coEvery {
                fileService.uploadFile(uploadedFilePath, file)
            } returns uploadedFilePath

            coEvery {
                permitRepository.getPermitById(permitId)
            } returns mockEntity

            //Act
            val result = assertFailsWith<CommonException> {
                permitService.editPermit(
                    permitId,
                    formHashMap,
                    fileHashMap
                )
            }

            //Assert
            assertEquals(expectedMessage, result.message)
            coVerify {
                fileService.uploadFile(uploadedFilePath, file)
            }
            coVerify(exactly = 0) {
                permitRepository.editPermit(listOf(permitId), body)
            }
        }

    @Test
    fun `editPermit() without attachment should throw correct exceptions when permit is not in pending status`() =
        runTest {
            //Arrange
            val permitId = "permitId"
            val formHashMap = hashMapOf<String, String>(
                "reason" to "idk"
            )
            val fileHashMap = hashMapOf<String, File>()
            val body = EditPermitBody(
                reason = "idk",
            )
            val expectedMessage = "Illegal permit status"

            val mockEntity = PermitByIdEntity(
                approvalStatus = ApprovalStatus.DECLINED
            )

            coEvery {
                permitRepository.getPermitById(permitId)
            } returns mockEntity


            //Act
            val result = assertFailsWith<CommonException> {
                permitService.editPermit(
                    permitId,
                    formHashMap,
                    fileHashMap
                )
            }

            //Assert
            assertEquals(expectedMessage, result.message)
            coVerify(exactly = 0) {
                permitRepository.editPermit(listOf(permitId), body)
            }
        }

    //endregion

    // --- existing tests above are unchanged ---

    //region cancelPermit test
    @Test
    fun `cancelPermit should throw notFoundException when permit is not found`() = runTest {
        //Arrange
        val permitIds = listOf("permitId")
        val queryParams = GetPermitQueryParams(listId = permitIds)
        val expectedMessage = "can't find permit data"

        coEvery { permitRepository.getPermitList(queryParams) } returns emptyList()

        //Act
        val result = assertFailsWith<CommonException> {
            permitService.cancelPermit(permitIds)
        }

        //Assert
        assertEquals(expectedMessage, result.message)
        coVerify { permitRepository.getPermitList(queryParams) }
        coVerify(exactly = 0) { permitRepository.editPermit(any(), any()) }
    }

    @Test
    fun `cancelPermit should throw illegalStatusExceptions when permit is not in pending status`() =
        runTest {
            //Arrange
            val permitIds = listOf("permitId")
            val queryParams = GetPermitQueryParams(listId = permitIds)
            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVED // Not pending
                )
            )
            val expectedMessage = "Illegal permit status"

            coEvery { permitRepository.getPermitList(queryParams) } returns mockEntity

            //Act
            val result = assertFailsWith<CommonException> {
                permitService.cancelPermit(permitIds)
            }

            //Assert
            assertEquals(expectedMessage, result.message)
            coVerify { permitRepository.getPermitList(queryParams) }
            coVerify(exactly = 0) { permitRepository.editPermit(any(), any()) }
        }

    @Test
    fun `cancelPermit should rollback attendance by deleting it for ABSENCE type`() = runTest {
        //Arrange
        val permitIds = listOf("permitId")
        val queryParams = GetPermitQueryParams(listId = permitIds)
        val mockPermitEntity = listOf(
            PermitListEntity(
                id = "permitId",
                approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                type = PermitType.ABSENCE
            )
        )
        val mockAttendanceList = listOf(
            AttendanceEntity(
                id = "att1",
                permit = AttendanceEntity.Permit(id = "permitId", type = PermitType.ABSENCE),
                student = AttendanceEntity.User(id = "student1")
            )
        )
        val expectedEditBody = EditPermitBody(approvalStatus = ApprovalStatus.CANCELED)
        val expectedResponse = Response<Unit>(
            status = 201,
            message = "permit data successfully updated",
        )

        coEvery { permitRepository.getPermitList(queryParams) } returns mockPermitEntity
        coEvery { attendanceRepository.getAttendanceListByPermit(permitIds) } returns mockAttendanceList
        coEvery { attendanceRepository.deleteAttendanceData(listOf("att1")) } just runs
        coEvery { permitRepository.editPermit(permitIds, expectedEditBody) } just runs

        //Act
        val result = permitService.cancelPermit(permitIds)

        //Assert
        assertEquals(expectedResponse, result)
        coVerify { attendanceRepository.deleteAttendanceData(listOf("att1")) }
        coVerify { permitRepository.editPermit(permitIds, expectedEditBody) }
    }

    @Test
    fun `cancelPermit should rollback attendance by deleting it for DISPENSATION type when checkedInAt is null`() =
        runTest {
            //Arrange
            val permitIds = listOf("permitId")
            val queryParams = GetPermitQueryParams(listId = permitIds)
            val mockPermitEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION
                )
            )
            val mockAttendanceList = listOf(
                AttendanceEntity(
                    id = "att1",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.DISPENSATION
                    ),
                    student = AttendanceEntity.User(id = "student1"),
                    checkedInAt = null // Key condition
                )
            )
            val expectedEditBody = EditPermitBody(approvalStatus = ApprovalStatus.CANCELED)
            val expectedResponse = Response<Unit>(
                status = 201,
                message = "permit data successfully updated",
            )

            coEvery { permitRepository.getPermitList(queryParams) } returns mockPermitEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(permitIds) } returns mockAttendanceList
            coEvery { attendanceRepository.deleteAttendanceData(listOf("att1")) } just runs
            coEvery { permitRepository.editPermit(permitIds, expectedEditBody) } just runs

            //Act
            val result = permitService.cancelPermit(permitIds)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify { attendanceRepository.deleteAttendanceData(listOf("att1")) }
            coVerify { permitRepository.editPermit(permitIds, expectedEditBody) }
        }

    @Test
    fun `cancelPermit should rollback attendance by updating status for DISPENSATION type when checkedInAt is not null`() =
        runTest {
            //Arrange
            val permitIds = listOf("permitId")
            val queryParams = GetPermitQueryParams(listId = permitIds)
            val checkInTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()
            val localTimeNowMockk =
                LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset()) // 09:00:00

            val mockPermitEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION
                )
            )
            val mockAttendanceList = listOf(
                AttendanceEntity(
                    id = "att1",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.DISPENSATION
                    ),
                    student = AttendanceEntity.User(id = "student1"),
                    checkedInAt = checkInTime // Key condition
                )
            )
            val expectedPermitEditBody = EditPermitBody(approvalStatus = ApprovalStatus.CANCELED)
            // Expect rollback to LATE status
            val expectedAttendanceEditBody = EditAttendanceBody(status = AttendanceStatus.LATE)
            val expectedResponse = Response<Unit>(
                status = 201,
                message = "permit data successfully updated",
            )

            // Arrange settings: 08:40 - 09:25 check-in, 08:55 school start
            // 09:00 check-in should be LATE
            arrangeGeneralSetting(
                generalSettingMock.copy(
                    checkInPeriodStart = localTimeNowMockk.minusMinutes(20), // 08:40
                    checkInPeriodEnd = localTimeNowMockk.plusMinutes(25),   // 09:25
                    schoolPeriodStart = localTimeNowMockk.minusMinutes(5) // 08:55
                )
            )

            coEvery { permitRepository.getPermitList(queryParams) } returns mockPermitEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(permitIds) } returns mockAttendanceList
            coEvery {
                attendanceRepository.updateAttendanceData(
                    listOf("att1"),
                    expectedAttendanceEditBody
                )
            } just runs
            coEvery { permitRepository.editPermit(permitIds, expectedPermitEditBody) } just runs

            //Act
            val result = permitService.cancelPermit(permitIds)

            //Assert
            assertEquals(expectedResponse, result)
            coVerify {
                attendanceRepository.updateAttendanceData(
                    listOf("att1"),
                    expectedAttendanceEditBody
                )
            }
            coVerify { permitRepository.editPermit(permitIds, expectedPermitEditBody) }
        }

    //endregion

    //region deletePermit test
    @Test
    fun `deletePermit should throw notFoundException when permit is not found`() = runTest {
        //Arrange
        val permitIds = listOf("permitId")
        val queryParams = GetPermitQueryParams(listId = permitIds)
        val expectedMessage = "can't find permit data"

        // Mock first transaction
        coEvery { permitRepository.getPermitList(queryParams) } returns emptyList()

        //Act
        val result = assertFailsWith<CommonException> {
            permitService.deletePermit(permitIds)
        }

        //Assert
        assertEquals(expectedMessage, result.message)
        coVerify { permitRepository.getPermitList(queryParams) }
        coVerify(exactly = 0) { permitRepository.deletePermit(any()) }
        coVerify(exactly = 0) { fileService.deleteFile(any<String>()) }
    }

    @Test
    fun `deletePermit should successfully delete permit and attachment file`() = runTest {
        //Arrange
        val permitIds = listOf("permitId1", "permitId2")
        val queryParams = GetPermitQueryParams(listId = permitIds)
        val attachments = listOf("path/to/file1.jpg", "path/to/file2.pdf")
        val mockPermits = listOf(
            PermitListEntity(id = "permitId1", attachment = "path/to/file1.jpg"),
            PermitListEntity(id = "permitId2", attachment = "path/to/file2.pdf")
        )

        val expectedResponse = Response<Unit>(
            status = 200,
            message = "permit data successfully deleted",
        )

        // Mock first transaction
        coEvery { permitRepository.getPermitList(queryParams) } returns mockPermits
        // Mock second transaction
        coEvery { permitRepository.deletePermit(permitIds) } just runs
        // Mock file deletion
        coEvery { fileService.deleteFile(attachments) } just runs

        //Act
        val result = permitService.deletePermit(permitIds)

        //Assert
        assertEquals(expectedResponse, result)
        coVerify { permitRepository.getPermitList(queryParams) }
        coVerify { permitRepository.deletePermit(permitIds) }
        coVerify { fileService.deleteFile(attachments) }
    }
    //endregion

    //region doApproval edge case
    @Test
    fun `doApproval should throw exception when rolling back DISPENSATION and checkInTime is outside period`() =
        runTest {
            //Arrange
            val queryParams = GetPermitQueryParams(
                listId = listOf("permitId"),
                isActive = true
            )
            val approverId: String = "approverId"
            val body = PermitApprovalBody(
                listId = listOf("permitId"),
                approvalNote = "Declined",
                isApproved = false // Declined
            )
            val checkInTime =
                OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()).toString()
            val localTimeNowMockk =
                LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset()) // 09:00:00

            val mockEntity = listOf(
                PermitListEntity(
                    id = "permitId",
                    approvalStatus = ApprovalStatus.APPROVAL_PENDING,
                    type = PermitType.DISPENSATION
                )
            )
            val mockAttendanceByPermit = listOf(
                AttendanceEntity(
                    id = "attendanceId",
                    permit = AttendanceEntity.Permit(
                        id = "permitId",
                        type = PermitType.DISPENSATION
                    ),
                    student = AttendanceEntity.User(id = "userId"),
                    checkedInAt = checkInTime
                )
            )

            // Arrange settings: Check-in is 09:30 - 10:00
            // The 09:00 time is OUTSIDE the period
            arrangeGeneralSetting(
                generalSettingMock.copy(
                    checkInPeriodStart = localTimeNowMockk.plusMinutes(30), // 09:30
                    checkInPeriodEnd = localTimeNowMockk.plusHours(1),   // 10:00
                    schoolPeriodStart = localTimeNowMockk.plusMinutes(40) // 09:40
                )
            )

            coEvery { permitRepository.getPermitList(queryParams = queryParams) } returns mockEntity
            coEvery { attendanceRepository.getAttendanceListByPermit(listOf("permitId")) } returns mockAttendanceByPermit
            coEvery { permitRepository.editPermit(any(), any()) } just runs

            //Act
            val result = assertFailsWith<CommonException> {
                permitService.doApproval(approverId, body)
            }

            //Assert
            // This exception bubbles up from getAttendanceStatus
            assertEquals("Outside time period", result.message)
            // Verifies that the permit edit was still called before the exception
            coVerify { permitRepository.editPermit(any(), any()) }
        }
    //endregion
}
