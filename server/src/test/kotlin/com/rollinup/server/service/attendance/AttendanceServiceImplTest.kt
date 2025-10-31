package com.rollinup.server.service.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.IllegalLocationException
import com.rollinup.server.MockkEnvironment
import com.rollinup.server.UnauthorizedTokenException
import com.rollinup.server.cache.generalsetting.GeneralSettingCache
import com.rollinup.server.cache.holiday.HolidayCache
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.datasource.database.model.attendance.AttendanceByClassEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceByStudentEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceEntity
import com.rollinup.server.datasource.database.model.attendance.AttendanceSummaryEntity
import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import com.rollinup.server.datasource.database.repository.attendance.AttendanceRepository
import com.rollinup.server.datasource.database.repository.permit.PermitRepository
import com.rollinup.server.mapper.AttendanceMapper
import com.rollinup.server.model.Role
import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.attendance.GetAttendanceByClassListResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByIdResponse
import com.rollinup.server.model.response.attendance.GetAttendanceByStudentListResponse
import com.rollinup.server.service.file.FileService
import com.rollinup.server.util.Message
import com.rollinup.server.util.Utils
import com.rollinup.server.util.manager.TransactionManager
import com.rollinup.server.util.successGettingResponse
import io.ktor.http.content.MultiPartData
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.Transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AttendanceServiceImplTest {

    @MockK
    private lateinit var attendanceRepository: AttendanceRepository


    @MockK
    private lateinit var permitRepository: PermitRepository

    @MockK
    private lateinit var fileService: FileService

    @MockK
    private lateinit var transactionManager: TransactionManager

    @MockK
    private lateinit var generalSettingCache: GeneralSettingCache

    @MockK
    private lateinit var holidayCache: HolidayCache

    @MockK
    private var mockMultiPartData: MultiPartData = mockk()

    private lateinit var attendanceService: AttendanceServiceImpl

    private val envMock = MockkEnvironment()

    private val mapper = AttendanceMapper()

    private var file = mockk<File>()

    private var validDateTimeInstant = Instant.parse("2025-10-28T09:00:00+07:00")

    private fun arrangeGeneralSetting(
        data: GeneralSetting,
    ) {
        coEvery {
            generalSettingCache.get()
        } returns data
    }

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

    @Before
    fun setUp() {
        envMock.setup()

        MockKAnnotations.init(this)

        attendanceService = AttendanceServiceImpl(
            attendanceRepository = attendanceRepository,
            permitRepository = permitRepository,
            fileService = fileService,
            mapper = mapper,
            transactionManager = transactionManager,
            generalSetting = generalSettingCache,
            holidayCache = holidayCache
        )

        arrangeSuspendTransaction()

        arrangeGeneralSetting(
            data = generalSettingMock
        )

        arrangeHoliday(
            data = emptyList()
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getAttendanceById should return correct response data`() = runTest {
        //Arrange
        val id = ""
        val mockkEntity = AttendanceEntity(
            id = "id",
            student = AttendanceEntity.User(
                id = "s_id",
                studentId = "studentId",
                username = "Lebron",
                name = "lebron",
                classX = "xx"
            ),
            status = AttendanceStatus.CHECKED_IN,
            checkedInAt = "2025-10-04T04:16:25.123456Z",
            createdAt = "2025-10-04T04:16:25.123456Z",
            updatedAt = "2025-10-04T04:16:25.123456Z",
            date = "2025-10-04",
            permit = null,
            location = AttendanceEntity.Location(
                latitude = 123.123,
                longitude = 123.123
            ),
            attachment = "attachment"
        )


        val expectedResponse = Response(
            status = 200,
            message = "attendance".successGettingResponse(),
            data = GetAttendanceByIdResponse(
                id = "id",
                student = GetAttendanceByIdResponse.User(
                    id = "s_id",
                    studentId = "studentId",
                    name = "lebron",
                    xClass = "xx"
                ),
                status = "checked_in",
                updatedAt = "2025-10-04T04:16:25.123456Z",
                createdAt = "2025-10-04T04:16:25.123456Z",
                checkedInAt = "2025-10-04T04:16:25.123456Z",
                permit = null
            )
        )

        coEvery {
            attendanceRepository.getAttendanceById(id)
        } returns mockkEntity

        //Act
        val response = attendanceService.getAttendanceById(id)

        //Assert
        coVerify {
            attendanceRepository.getAttendanceById(id)
        }

        assertEquals(expectedResponse, response)
    }

    @Test
    fun `getAttendanceById should throw correct exception when data not found`() = runTest {
        //Arrange
        val id = "yes"
        val expectedMessage = "can't find attendance data"

        coEvery {
            attendanceRepository.getAttendanceById(id)
        } returns null

        //Act
        val exception = assertFailsWith<CommonException> {
            attendanceService.getAttendanceById(id)
        }

        //Assert
        coVerify {
            attendanceRepository.getAttendanceById(id)
        }

        assertEquals(expectedMessage, exception.message)
    }

    @Test
    fun `getAttendanceListByStudent should return correctList`() = runTest {
        //Arrange
        val studentId = "studentId"
        val queryParams = GetAttendanceByStudentQueryParams(
            search = "search",
            limit = 100,
            page = 1,
            dateRange = listOf(123123123, 123123123)
        )

        val mockkEntity = (0..10).map {
            AttendanceByStudentEntity(
                id = "id$it",
                status = AttendanceStatus.entries.random(),
                checkedInAt = "2025-10-04T04:16:25.123456Z",
                date = "2025-10-04",
                permit = null,
                createdAt = "2025-10-04T04:16:25.123456Z",
                updatedAt = "2025-10-04T04:16:25.123456Z"
            )
        }

        val mockKSummary = AttendanceSummaryEntity(
            checkedIn = 1,
            sick = 2,
            other = 3,
            late = 4,
            absent = 5,
            excused = 6,
            approvalPending = 7
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting attendance data",
            data = GetAttendanceByStudentListResponse(
                record = 11,
                page = 1,
                summary = GetAttendanceByStudentListResponse.Summary(
                    checkedIn = 1,
                    late = 4,
                    excused = 6,
                    approvalPending = 7,
                    absent = 5,
                    sick = 2,
                    other = 3
                ),
                data = mockkEntity.map {
                    GetAttendanceByStudentListResponse.GetAttendanceByStudentListDTO(
                        id = it.id,
                        status = it.status,
                        checkedInAt = "2025-10-04T04:16:25.123456Z",
                        permit = null,
                        createdAt = "2025-10-04T04:16:25.123456Z",
                        updatedAt = "2025-10-04T04:16:25.123456Z"
                    )
                }
            )
        )

        coEvery {
            attendanceRepository.getAttendanceListByStudent(queryParams, studentId)
        } returns mockkEntity

        coEvery {
            attendanceRepository.getSummary(
                studentId = studentId,
                dateRange = queryParams.dateRange
            )
        } returns mockKSummary


        //Act
        val result = attendanceService.getAttendanceListByStudent(queryParams, studentId)

        //Assert
        coVerify {
            attendanceRepository.getAttendanceListByStudent(queryParams, studentId)
            attendanceRepository.getSummary(
                studentId = studentId,
                dateRange = queryParams.dateRange
            )
        }

        assert(result.data!!.data.isNotEmpty())
        assertEquals(expectedResponse, result)
    }

    @Test
    fun `getAttendanceListByClass should return correct list `() = runTest {
        //Arrange
        val queryParams = GetAttendanceByClassQueryParams(
            limit = 100,
            page = 1,
            date = 69
        )

        val classKey = 69

        val expectedList = (0..4).map {
            AttendanceByClassEntity(
                user = AttendanceByClassEntity.User(
                    id = "id$it",
                    name = "name$it",
                    studentId = "studentId$it",
                    username = "username$it"
                ),
                attendance = AttendanceByClassEntity.Attendance(
                    id = "attId$it",
                    status = AttendanceStatus.CHECKED_IN,
                    checkedInAt = "2025-10-04T04:16:25.123456Z",
                    date = "2025-10-04"
                ),
                permit = null
            )
        }

        val expectedSummary = AttendanceSummaryEntity(
            checkedIn = 1,
            sick = 2,
            other = 3,
            late = 4,
            absent = 5,
            excused = 6,
            approvalPending = 7
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting attendance data",
            data = GetAttendanceByClassListResponse(
                record = 5,
                page = 1,
                summary = GetAttendanceByClassListResponse.Summary(
                    checkedIn = 1,
                    sick = 2,
                    other = 3,
                    late = 4,
                    absent = 5,
                    excused = 6,
                    approvalPending = 7
                ),
                data = expectedList.map {
                    GetAttendanceByClassListResponse.GetAttendanceByClassListDTO(
                        student = GetAttendanceByClassListResponse.User(
                            id = it.user.id,
                            studentId = it.user.studentId,
                            name = it.user.name
                        ),
                        attendance = GetAttendanceByClassListResponse.Attendance(
                            id = it.attendance!!.id,
                            checkedInAt = "2025-10-04T04:16:25.123456Z",
                            status = AttendanceStatus.CHECKED_IN.value,
                            date = "2025-10-04"
                        ),
                        permit = null
                    )
                }
            )
        )

        coEvery {
            attendanceRepository.getAttendanceListByClass(queryParams, classKey)
        } returns expectedList

        coEvery {
            attendanceRepository.getSummary(classKey = classKey, dateRange = listOf(69, 69))
        } returns expectedSummary

        //Act
        val response = attendanceService.getAttendanceListByClass(queryParams, classKey)

        //Assert
        coVerify {
            attendanceRepository.getAttendanceListByClass(queryParams, classKey)
            attendanceRepository.getSummary(classKey = classKey, dateRange = listOf(69, 69))
        }
        assertEquals(5, expectedResponse.data!!.data.size)
        assertEquals(expectedResponse, response)
    }

    /**
     * This test verifies that `createAttendanceData` correctly processes a `MultiPartData` request.
     * To test this, we construct a `MultiPartData` object containing form items and a file item.
     * Since `MultiPartData` is an interface that extends `Flow<PartData>`, we can create a test
     * implementation by delegating the `Flow` implementation to `flowOf()`.
     */
    @Test
    fun `createAttendance at correct times with correct data should return correct response and create checked_in attendance data`() =
        runTest {
            // Arrange
            val studentUserId = "student123"
            val role = Role.STUDENT
            val latitude = "12.2222"
            val longitude = "123.123"
            val currentDate = LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString().replace("-","")
            val uploadedFilePath = "UPLOAD_DIR/attachment/attendance/$currentDate/some-file.jpg"
            val attachment = file

            val localTime = LocalTime.ofInstant(
                validDateTimeInstant,
                Utils.getOffset()
            )

            val checkInTime = OffsetDateTime
                .ofInstant(
                    validDateTimeInstant,
                    Utils.getOffset()
                )
                .toInstant()
                .toEpochMilli()

            val formHashMap = hashMapOf(
                "studentUserId" to studentUserId,
                "latitude" to latitude,
                "longitude" to longitude,
                "checkedInAt" to checkInTime.toString()
            )

            val fileHashMap = hashMapOf(
                "attachment" to attachment
            )

            val expectedResponse = Response<Unit>(
                status = 201,
                message = "attendance data successfully created",
            )

            arrangeMockkNow(
                time = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset())
            )

            arrangeGeneralSetting(
                data = generalSettingMock.copy(
                    checkInPeriodStart = localTime.minusHours(1),
                    checkInPeriodEnd = localTime.plusMinutes(20),
                    schoolPeriodStart = localTime.plusMinutes(10),
                    lat = 12.2222,
                    long = 123.123,
                    rad = 1000.000
                )
            )

            coEvery {
                attendanceRepository.getAttendanceListByStudent(
                    queryParams = any(),
                    studentId = studentUserId
                )
            } returns emptyList()

            coEvery {
                file.name
            } returns "some-file.jpg"

            coEvery {
                fileService.uploadFile(filePath = uploadedFilePath, file = file)
            } returns uploadedFilePath

            coEvery {
                attendanceRepository.createAttendanceData(
                    match {
                        it.studentUserId == "student123" &&
                                it.latitude == 12.2222 &&
                                it.longitude == 123.123 &&
                                it.attachment.endsWith("some-file.jpg") &&
                                it.status == AttendanceStatus.CHECKED_IN
                    }
                )
            } returns "attendance_id"

            //Act
            val result = attendanceService.createAttendanceData(
                userId = studentUserId,
                role = role,
                formHashMap = formHashMap,
                fileHashMap = fileHashMap
            )

            //Assert
            coVerify {
                attendanceRepository.getAttendanceListByStudent(
                    queryParams = any(),
                    studentId = studentUserId
                )
            }

            coVerify {
                attendanceRepository.createAttendanceData(
                    match {
                        it.studentUserId == "student123" &&
                                it.latitude == 12.2222 &&
                                it.longitude == 123.123 &&
                                it.attachment.endsWith("some-file.jpg") &&
                                it.status == AttendanceStatus.CHECKED_IN
                    }
                )
            }

            assertEquals(expectedResponse, result)
        }

    @Test
    fun `createAttendance should create attendance data with late status when checkintime is after schoolperiodstart and before checkinperiodend`() =
        runTest {
            //Arrange
            val studentUserId = "student123"
            val role = Role.STUDENT
            val latitude = "12.2222"
            val longitude = "123.123"
            val currentDate = LocalDate.now().toString().replace("-", "")
            val uploadedFilePath = "UPLOAD_DIR/attachment/attendance/$currentDate/some-file.jpg"
            val attachment = file

            val localTime = OffsetDateTime
                .ofInstant(
                    Instant.now(),
                    Utils.getOffset()
                )
                .toLocalTime()

            val checkInTime = OffsetDateTime
                .ofInstant(
                    Instant.now(),
                    Utils.getOffset()
                )
                .toInstant()
                .toEpochMilli()

            val formHashMap = hashMapOf(
                "studentUserId" to studentUserId,
                "latitude" to latitude,
                "longitude" to longitude,
                "checkedInAt" to checkInTime.toString()
            )

            val fileHashMap = hashMapOf(
                "attachment" to attachment
            )

            val expectedResponse = Response<Unit>(
                status = 201,
                message = "attendance data successfully created",
            )

            arrangeGeneralSetting(
                data = generalSettingMock.copy(
                    checkInPeriodStart = localTime.minusHours(1),
                    checkInPeriodEnd = localTime.plusMinutes(20),
                    schoolPeriodStart = localTime.minusMinutes(10),
                    lat = 12.2222,
                    long = 123.123,
                    rad = 1000.000
                )
            )

            coEvery {
                attendanceRepository.getAttendanceListByStudent(
                    queryParams = any(),
                    studentId = studentUserId
                )
            } returns emptyList()

            coEvery {
                file.name
            } returns "some-file.jpg"

            coEvery {
                fileService.uploadFile(filePath = uploadedFilePath, file = file)
            } returns uploadedFilePath

            coEvery {
                attendanceRepository.createAttendanceData(
                    match {
                        it.studentUserId == "student123" &&
                                it.latitude == 12.2222 &&
                                it.longitude == 123.123 &&
                                it.attachment.endsWith("some-file.jpg") &&
                                it.status == AttendanceStatus.LATE
                    }
                )
            } returns "attendance_id"

            //Act
            val result = attendanceService.createAttendanceData(
                userId = studentUserId,
                role = role,
                formHashMap = formHashMap,
                fileHashMap = fileHashMap
            )

            //Assert
            coVerify {
                attendanceRepository.getAttendanceListByStudent(
                    queryParams = any(),
                    studentId = studentUserId
                )
            }

            coVerify {
                attendanceRepository.createAttendanceData(
                    match {
                        it.studentUserId == "student123" &&
                                it.latitude == 12.2222 &&
                                it.longitude == 123.123 &&
                                it.attachment.endsWith("some-file.jpg") &&
                                it.status == AttendanceStatus.LATE
                    }
                )
            }

            assertEquals(expectedResponse, result)
        }


    @Test
    fun `createAttendance should throw exceptions when userrole is student and studentUserId between formData and argument are different`() =
        runTest {
            //Arrange
            val studentUserId = "student123"
            val falseUserId = "falseUser"
            val role = Role.STUDENT
            val latitude = "12.2222"
            val longitude = "123.123"
            val attachment = file
            val localTime = OffsetDateTime
                .ofInstant(
                    Instant.now(),
                    Utils.getOffset()
                )
                .toLocalTime()

            val checkInTime = OffsetDateTime
                .ofInstant(
                    Instant.now(),
                    Utils.getOffset()
                )
                .toInstant()
                .toEpochMilli()

            val formHashMap = hashMapOf(
                "studentUserId" to studentUserId,
                "latitude" to latitude,
                "longitude" to longitude,
                "checkedInAt" to checkInTime.toString()
            )

            val fileHashMap = hashMapOf(
                "attachment" to attachment
            )

            val expectedResult = IllegalArgumentException("studentId")

            arrangeGeneralSetting(
                data = generalSettingMock.copy(
                    checkInPeriodStart = localTime.minusHours(1),
                    checkInPeriodEnd = localTime.plusMinutes(20),
                    schoolPeriodStart = localTime.plusMinutes(10),
                    lat = 12.2222,
                    long = 123.123,
                    rad = 1000.000
                )
            )

            //Act & Assert
            val result = assertFailsWith<IllegalArgumentException> {
                attendanceService.createAttendanceData(
                    userId = falseUserId,
                    role = role,
                    formHashMap = formHashMap,
                    fileHashMap = fileHashMap
                )
            }

            assertEquals("studentId", result.message)
        }

    @Test
    fun `createAttendance should throw exceptions when userId is blank`() = runTest {
        //Arrange
        val userId = ""
        val role = Role.STUDENT
        val formHash = hashMapOf<String, String>()
        val fileHash = hashMapOf<String, File>()

        //Act & Assert
        val result = assertFailsWith<UnauthorizedTokenException> {
            attendanceService.createAttendanceData(
                userId = userId,
                role = role,
                formHashMap = formHash,
                fileHashMap = fileHash,
            )
        }
    }


    @Test
    fun `createAttendance should throw exceptions when formHash is empty`() = runTest {
        //Arrange
        val userId = "userId"
        val role = Role.STUDENT
        val expectedMessage = "Invalid request body"
        val formHash = hashMapOf<String, String>()
        val fileHash = hashMapOf(
            "attachment" to file
        )

        //Act
        val result = assertFailsWith<CommonException> {
            attendanceService.createAttendanceData(
                userId = userId,
                role = role,
                formHashMap = formHash,
                fileHashMap = fileHash,
            )
        }

        //Assert
        assertEquals(expectedMessage, result.message)
    }


    @Test
    fun `createAttendance should throw exceptions when fileHash is empty`() = runTest {
        //Arrange
        val userId = "userId"
        val role = Role.STUDENT
        val expectedMessage = "failed to upload attendance attachment file"
        val latitude = "12.2222"
        val longitude = "123.123"

        val checkInTime = OffsetDateTime
            .ofInstant(
                Instant.now(),
                Utils.getOffset()
            )
            .toInstant()
            .toEpochMilli()

        val fileHashMap = hashMapOf<String, File>()
        val formHashMap = hashMapOf(
            "studentUserId" to userId,
            "latitude" to latitude,
            "longitude" to longitude,
            "checkedInAt" to checkInTime.toString()
        )

        //Act
        val result = assertFailsWith<CommonException> {
            attendanceService.createAttendanceData(
                userId = userId,
                role = role,
                formHashMap = formHashMap,
                fileHashMap = fileHashMap,
            )
        }

        //Assert
        assertEquals(expectedMessage, result.message)
    }

    @Test
    fun `createAttendance should throw exceptions when mandatory field on formHashMap is invalid`() =
        runTest {
            //Arrange
            val userId = "userId"
            val role = Role.STUDENT
            val fileHashMap = hashMapOf(
                "attachment" to file
            )
            val formHashMap = hashMapOf<String, String>(
                "anything but mandatory field" to "yes"
            )

            val localTime = LocalTime.now()

            arrangeGeneralSetting(
                data = generalSettingMock.copy(
                    checkInPeriodStart = localTime.minusHours(1),
                    checkInPeriodEnd = localTime.plusMinutes(20),
                    schoolPeriodStart = localTime.plusMinutes(10),
                    lat = 12.2222,
                    long = 123.123,
                    rad = 1000.000
                )
            )

            //Assert
            assertFailsWith<CommonException> {
                attendanceService.createAttendanceData(
                    userId = userId,
                    role = role,
                    formHashMap = formHashMap,
                    fileHashMap = fileHashMap
                )
            }
        }

    @Test
    fun `createAttendance should throw exceptions when location is outside calculated boundaries`() =
        runTest {
            // Arrange
            val studentUserId = "student123"
            val role = Role.STUDENT
            val latitude = "12.2222"
            val longitude = "123.123"
            val currentDate = LocalDate.now().toString().replace("-", "")
            val attachment = file

            val localTime = OffsetDateTime
                .ofInstant(
                    Instant.now(),
                    Utils.getOffset()
                )
                .toLocalTime()

            val checkInTime = OffsetDateTime
                .ofInstant(
                    Instant.now(),
                    Utils.getOffset()
                )
                .toInstant()
                .toEpochMilli()

            val formHashMap = hashMapOf(
                "studentUserId" to studentUserId,
                "latitude" to latitude,
                "longitude" to longitude,
                "checkedInAt" to checkInTime.toString()
            )

            val fileHashMap = hashMapOf(
                "attachment" to attachment
            )

            arrangeGeneralSetting(
                data = generalSettingMock.copy(
                    checkInPeriodStart = localTime.minusHours(1),
                    checkInPeriodEnd = localTime.plusMinutes(20),
                    schoolPeriodStart = localTime.plusMinutes(10),
                    lat = -89.2222,
                    long = 110.123,
                    rad = 0.1
                )
            )

            //Act & Assert
            assertFailsWith<IllegalLocationException> {
                attendanceService.createAttendanceData(
                    userId = studentUserId,
                    role = role,
                    formHashMap = formHashMap,
                    fileHashMap = fileHashMap
                )
            }
        }


    // --- Additional tests for createAttendanceData ---

    @Test
    fun `createAttendance should create attendance data with CHECKED_IN status when checkintime is before schoolperiodstart`() =
        runTest {
            val studentUserId = "student123"
            val role = Role.STUDENT
            val latitude = "12.2222"
            val longitude = "123.123"

            // This time is 09:00:00
            arrangeMockkNow(time = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()))

            val currentDate = LocalDate.ofInstant(validDateTimeInstant, Utils.getOffset()).toString().replace("-", "")
            val uploadedFilePath = "UPLOAD_DIR/attachment/attendance/$currentDate/some-file.jpg"
            val attachment = file
            val localTime = LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset())
            val checkInTime = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()).toInstant().toEpochMilli()

            val formHashMap = hashMapOf(
                "studentUserId" to studentUserId,
                "latitude" to latitude,
                "longitude" to longitude,
                "checkedInAt" to checkInTime.toString()
            )

            val fileHashMap = hashMapOf("attachment" to attachment)

            val expectedResponse = Response<Unit>(status = 201, message = "attendance data successfully created")

            // General setting: Check-in is 08:00 to 09:30, school starts at 09:10
            // The check-in time of 09:00 should be CHECKED_IN
            arrangeGeneralSetting(
                data = generalSettingMock.copy(
                    checkInPeriodStart = localTime.minusHours(1), // 08:00
                    checkInPeriodEnd = localTime.plusMinutes(30),  // 09:30
                    schoolPeriodStart = localTime.plusMinutes(10), // 09:10
                    lat = 12.2222,
                    long = 123.123,
                    rad = 1000.000
                )
            )

            coEvery { attendanceRepository.getAttendanceListByStudent(any(), studentUserId) } returns emptyList()
            coEvery { file.name } returns "some-file.jpg"
            coEvery { fileService.uploadFile(any(), file) } returns uploadedFilePath
            coEvery {
                attendanceRepository.createAttendanceData(match {
                    it.studentUserId == "student123" &&
                            it.status == AttendanceStatus.CHECKED_IN // Verify CHECKED_IN
                })
            } returns "attendance_id"

            val result = attendanceService.createAttendanceData(studentUserId, role, formHashMap, fileHashMap)
            assertEquals(expectedResponse, result)
        }

    @Test
    fun `createAttendance should throw exception when check-in is on a weekend`() = runTest {
        val studentUserId = "student123"
        val role = Role.STUDENT
        val latitude = "12.2222"
        val longitude = "123.123"
        val attachment = file

        // 2025-10-25 is a Saturday
        val weekendInstant = Instant.parse("2025-10-25T09:00:00+07:00")
        arrangeMockkNow(time = OffsetDateTime.ofInstant(weekendInstant, Utils.getOffset()))
        val checkInTime = weekendInstant.toEpochMilli()

        val formHashMap = hashMapOf(
            "studentUserId" to studentUserId,
            "latitude" to latitude,
            "longitude" to longitude,
            "checkedInAt" to checkInTime.toString()
        )
        val fileHashMap = hashMapOf("attachment" to attachment)

        val result = assertFailsWith<CommonException> {
            attendanceService.createAttendanceData(studentUserId, role, formHashMap, fileHashMap)
        }
        assertEquals(Message.OUTSIDE_TIME_PERIOD, result.message)
    }

    @Test
    fun `createAttendance should throw exception when check-in is on a holiday`() = runTest {
        val studentUserId = "student123"
        val role = Role.STUDENT
        val latitude = "12.2222"
        val longitude = "123.123"
        val attachment = file

        // Use validDateTimeInstant (2025-10-28)
        arrangeMockkNow(time = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()))
        val checkInTime = validDateTimeInstant.toEpochMilli()

        // Mark 2025-10-28 as a holiday
        arrangeHoliday(data = listOf(LocalDate.of(2025, 10, 28)))

        val formHashMap = hashMapOf(
            "studentUserId" to studentUserId,
            "latitude" to latitude,
            "longitude" to longitude,
            "checkedInAt" to checkInTime.toString()
        )
        val fileHashMap = hashMapOf("attachment" to attachment)

        val result = assertFailsWith<CommonException> {
            attendanceService.createAttendanceData(studentUserId, role, formHashMap, fileHashMap)
        }
        assertEquals(Message.OUTSIDE_TIME_PERIOD, result.message)
    }

    @Test
    fun `createAttendance should throw exception when checkintime is outside check-in period`() = runTest {
        val studentUserId = "student123"
        val role = Role.STUDENT
        val latitude = "12.2222"
        val longitude = "123.123"
        val attachment = file

        // This time is 09:00:00
        arrangeMockkNow(time = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()))
        val localTime = LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset())
        val checkInTime = validDateTimeInstant.toEpochMilli()

        val formHashMap = hashMapOf(
            "studentUserId" to studentUserId,
            "latitude" to latitude,
            "longitude" to longitude,
            "checkedInAt" to checkInTime.toString()
        )
        val fileHashMap = hashMapOf("attachment" to attachment)

        // General setting: Check-in is 09:30 to 10:00
        // The check-in time of 09:00 is too early
        arrangeGeneralSetting(
            data = generalSettingMock.copy(
                checkInPeriodStart = localTime.plusMinutes(30), // 09:30
                checkInPeriodEnd = localTime.plusHours(1),  // 10:00
                schoolPeriodStart = localTime.plusMinutes(40), // 09:40
                lat = 12.2222,
                long = 123.123,
                rad = 1000.000
            )
        )

        val result = assertFailsWith<CommonException> {
            attendanceService.createAttendanceData(studentUserId, role, formHashMap, fileHashMap)
        }
        // This exception comes from the private getAttendanceStatus function
        assertEquals(Message.OUTSIDE_TIME_PERIOD, result.message)
    }

    @Test
    fun `createAttendance should throw exception when attendance data for the day already exists`() = runTest {
        val studentUserId = "student123"
        val role = Role.STUDENT
        val latitude = "12.2222"
        val longitude = "123.123"

        arrangeMockkNow(time = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()))

        val attachment = file
        val localTime = LocalTime.ofInstant(validDateTimeInstant, Utils.getOffset())
        val checkInTime = OffsetDateTime.ofInstant(validDateTimeInstant, Utils.getOffset()).toInstant().toEpochMilli()

        val formHashMap = hashMapOf(
            "studentUserId" to studentUserId,
            "latitude" to latitude,
            "longitude" to longitude,
            "checkedInAt" to checkInTime.toString()
        )

        val fileHashMap = hashMapOf("attachment" to attachment)

        arrangeGeneralSetting(
            data = generalSettingMock.copy(
                checkInPeriodStart = localTime.minusHours(1),
                checkInPeriodEnd = localTime.plusMinutes(20),
                schoolPeriodStart = localTime.plusMinutes(10),
                lat = 12.2222,
                long = 123.123,
                rad = 1000.000
            )
        )

        // Mock that attendance for this student today already exists
        coEvery {
            attendanceRepository.getAttendanceListByStudent(any(), studentUserId)
        } returns listOf(mockk<AttendanceByStudentEntity>())

        val result = assertFailsWith<CommonException> {
            attendanceService.createAttendanceData(studentUserId, role, formHashMap, fileHashMap)
        }
        assertEquals("attendance data is already exist", result.message)
    }

    // --- Tests for updateAttendance ---

    @Test
    fun `updateAttendance should update status to CHECKED_IN`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val formHashMap = hashMapOf(
            "status" to "CHECKED_IN",
            "checkedInAt" to "1678842000000",
            "updatedBy" to editBy
        )
        val fileHashMap = hashMapOf<String, File>()

        coEvery { attendanceRepository.updateAttendanceData(listOf(attendanceId), any()) } returns Unit
        coEvery { attendanceRepository.updatePermit(attendanceId, null) } returns Unit

        val result = attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)

        assertEquals("Attendance data successfully updated", result.message)
        coVerify { attendanceRepository.updateAttendanceData(listOf(attendanceId), any()) }
        coVerify { attendanceRepository.updatePermit(attendanceId, null) }
    }

    @Test
    fun `updateAttendance should update status to LATE`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val formHashMap = hashMapOf(
            "status" to "LATE",
            "checkedInAt" to "1678842000000",
            "updatedBy" to editBy
        )
        val fileHashMap = hashMapOf<String, File>()

        coEvery { attendanceRepository.updateAttendanceData(listOf(attendanceId), any()) } returns Unit
        coEvery { attendanceRepository.updatePermit(attendanceId, null) } returns Unit

        val result = attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)

        assertEquals("Attendance data successfully updated", result.message)
        coVerify { attendanceRepository.updateAttendanceData(listOf(attendanceId), any()) }
        coVerify { attendanceRepository.updatePermit(attendanceId, null) }
    }

    @Test
    fun `updateAttendance should update status to ALPHA (delete record)`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val formHashMap = hashMapOf("status" to "ALPHA")
        val fileHashMap = hashMapOf<String, File>()

        coEvery { attendanceRepository.deleteAttendanceData(listOf(attendanceId)) } returns Unit

        val result = attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)

        assertEquals("Attendance data successfully updated", result.message)
        coVerify { attendanceRepository.deleteAttendanceData(listOf(attendanceId)) }
    }

    @Test
    fun `updateAttendance should update status to ABSENT by creating a permit`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val instantDateLong = validDateTimeInstant.toEpochMilli()
        val studentId = "student123"
        val formHashMap = hashMapOf(
            "status" to "ABSENT",
            "description" to "Sick",
            "duration" to "[$instantDateLong,$instantDateLong]",
            "updatedBy" to editBy
        )
        val fileHashMap = hashMapOf("attachment" to file)
        val mockAttendance = mockk<AttendanceEntity>(relaxed = true)

        coEvery { file.name } returns "sick-note.pdf"
        coEvery { fileService.uploadFile(any(), file) } returns "path/to/sick-note.pdf"
        coEvery { attendanceRepository.getAttendanceById(attendanceId) } returns mockAttendance
        coEvery { mockAttendance.student.id } returns studentId
        coEvery { permitRepository.createPermit(any()) } returns "permit123"
        coEvery {
            attendanceRepository.createAttendanceFromPermit(
                permitId = "permit123",
                studentId = studentId,
                dates = any(),
                status = AttendanceStatus.ABSENT
            )
        } returns Unit

        val result = attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)

        assertEquals("Attendance data successfully updated", result.message)
        coVerify { permitRepository.createPermit(match { it.studentId == studentId && it.attachment.endsWith("sick-note.pdf") }) }
        coVerify { attendanceRepository.createAttendanceFromPermit(any(), any(), any(), AttendanceStatus.ABSENT) }
    }

    @Test
    fun `updateAttendance should update status to EXCUSED by creating a permit`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val studentId = "student123"
        val dateLong = validDateTimeInstant.toEpochMilli()
        val formHashMap = hashMapOf(
            "status" to "EXCUSED",
            "description" to "Family event",
            "duration" to "[$dateLong,$dateLong]",
            "updatedBy" to editBy
        )
        val fileHashMap = hashMapOf("attachment" to file)
        val mockAttendance = mockk<AttendanceEntity>(relaxed = true)

        coEvery { file.name } returns "event.pdf"
        coEvery { fileService.uploadFile(any(), file) } returns "path/to/event.pdf"
        coEvery { attendanceRepository.getAttendanceById(attendanceId) } returns mockAttendance
        coEvery { mockAttendance.student.id } returns studentId
        coEvery { permitRepository.createPermit(any()) } returns "permit124"
        coEvery {
            attendanceRepository.createAttendanceFromPermit(
                permitId = "permit124",
                studentId = studentId,
                dates = any(),
                status = AttendanceStatus.EXCUSED
            )
        } returns Unit

        val result = attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)

        assertEquals("Attendance data successfully updated", result.message)
        coVerify { permitRepository.createPermit(match { it.studentId == studentId && it.attachment.endsWith("event.pdf") }) }
        coVerify { attendanceRepository.createAttendanceFromPermit(any(), any(), any(), AttendanceStatus.EXCUSED) }
    }

    @Test
    fun `updateAttendance should throw exception for invalid or missing status`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val formHashMap = hashMapOf("noStatus" to "null")
        val fileHashMap = hashMapOf<String, File>()

        val result = assertFailsWith<CommonException> {
            attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)
        }
        assertEquals(Message.INVALID_REQUEST_BODY, result.message)

        val emptyFormHashMap = hashMapOf<String, String>()
        val result2 = assertFailsWith<CommonException> {
            attendanceService.updateAttendance(attendanceId, editBy, emptyFormHashMap, fileHashMap)
        }
        assertEquals(Message.INVALID_REQUEST_BODY, result2.message)
    }

    @Test
    fun `updateAttendance should throw exception when updating to ABSENT and file is missing`() = runTest {
        val attendanceId = "att123"
        val editBy = "admin456"
        val formHashMap = hashMapOf(
            "status" to "ABSENT",
            "description" to "Sick",
            "duration" to "2025-10-30,2025-10-31",
            "updatedBy" to editBy
        )
        // File is missing
        val fileHashMap = hashMapOf<String, File>()
        val expectedMessage = "failed to upload permit attachment file"

        val result = assertFailsWith<CommonException> {
            attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)
        }
        assertEquals(expectedMessage, result.message)
    }

    @Test
    fun `updateAttendance should throw notFoundException when studentId is not found for permit`() =
        runTest {
            val attendanceId = "att123"
            val editBy = "admin456"
            val formHashMap = hashMapOf(
                "status" to "ABSENT",
                "description" to "Sick",
                "duration" to "2025-10-30,2025-10-31",
                "updatedBy" to editBy
            )
            val fileHashMap = hashMapOf("attachment" to file)
            val expectedMessage = "can't find student id data"

            coEvery { file.name } returns "sick-note.pdf"
            coEvery { fileService.uploadFile(any(), file) } returns "path/to/sick-note.pdf"
            // Mock getAttendanceById to return null
            coEvery { attendanceRepository.getAttendanceById(attendanceId) } returns null

            val result = assertFailsWith<CommonException> {
                attendanceService.updateAttendance(attendanceId, editBy, formHashMap, fileHashMap)
            }

            assertEquals(expectedMessage, result.message)
        }


}