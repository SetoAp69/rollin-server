package com.rollinup.server.service.generalsetting

import com.rollinup.server.CommonException
import com.rollinup.server.MockkEnvironment
import com.rollinup.server.datasource.database.model.generalsetting.GeneralSetting
import com.rollinup.server.datasource.database.repository.generalsetting.GeneralSettingRepository
import com.rollinup.server.mapper.GeneralSettingMapper
import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.generalsetting.GetGeneralSettingResponse
import com.rollinup.server.util.manager.TransactionManager
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.v1.core.Transaction
import org.junit.After
import org.junit.Before
import java.time.LocalTime
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GeneralSettingServiceImplTest {

    private lateinit var generalSettingService: GeneralSettingService

    @MockK
    private lateinit var generalSettingRepository: GeneralSettingRepository

    @MockK
    private lateinit var transactionManager: TransactionManager

    private val mapper = GeneralSettingMapper()

    private val envMock = MockkEnvironment()

    private fun arrangeSuspendTransaction() {
        coEvery {
            transactionManager.suspendTransaction<Any>(any())
        } answers {
            val block = args.first() as Function1<Transaction, Any>
            val mockedTransaction = mockk<Transaction>(relaxed = true)

            block.invoke(mockedTransaction)
        }
    }

    @Before
    fun setUp() {
        envMock.setup()

        MockKAnnotations.init(this)

        arrangeSuspendTransaction()

        generalSettingService = GeneralSettingServiceImpl(
            generalSettingRepository = generalSettingRepository,
            transactionManager = transactionManager,
            mapper = mapper
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getGeneralSetting() should return correct response`() = runTest {
        //Arrange
        val dateTime = OffsetDateTime.now()
        val time = LocalTime.now()

        val expectedEntity = GeneralSetting(
            semesterEnd = dateTime,
            semesterStart = dateTime,
            checkInPeriodStart = time,
            checkInPeriodEnd = time,
            schoolPeriodStart = time,
            schoolPeriodEnd = time,
            rad = 99.00,
            long = 1231.123123,
            lat = 123123.123123,
            updatedAt = dateTime,
            modifiedBy = "123",
            modifiedByName = "lebron"
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting general setting data",
            data = GetGeneralSettingResponse(
                semesterStart = dateTime.toString(),
                semesterEnd = dateTime.toString(),
                updatedAt = dateTime.toString(),
                schoolPeriodStart = time.toString(),
                schoolPeriodEnd = time.toString(),
                checkInPeriodStart = time.toString(),
                checkInPeriodEnd = time.toString(),
                latitude = 123123.123123,
                longitude = 1231.123123,
                radius = 99.00,
                modifiedBy = GetGeneralSettingResponse.ModifiedBy(
                    id = "123",
                    name = "lebron"
                )
            )
        )

        coEvery {
            generalSettingRepository.getGeneralSetting()
        } returns expectedEntity

        //Act
        val result = generalSettingService.getGeneralSetting()

        //Assert
        coVerify {
            generalSettingRepository.getGeneralSetting()
        }

        assertEquals(200, result.status)
        assertEquals(expectedResponse, result)
        assert(result.data != null)
    }

    @Test
    fun `getGeneralSetting() should throw correct exceptions when didn't found any data`() =
        runTest {
            //Arrange
            val expectedMessage = "can't find general setting data"

            coEvery {
                generalSettingRepository.getGeneralSetting()
            } returns null

            //Act
            val result = assertFailsWith<CommonException> {
                generalSettingService.getGeneralSetting()
            }

            //Assert

            coVerify {
                generalSettingRepository.getGeneralSetting()
            }

            assertEquals(expectedMessage, result.message)
        }


    @Test
    fun `updateGeneralSetting should update general setting with given value and return correct response`() =
        runTest {
            //Arrange
            val body = EditGeneralSettingBody(
                semesterStart = 12,
                semesterEnd = 13,
                schoolPeriodStart = 14,
                schoolPeriodEnd = 142,
                checkInPeriodStart = 121,
                checkInPeriodEnd = 214214,
                latitude = 11.11,
                longitude = 22.22,
                radius = 54.4
            )

            val editBy = "id"

            val expectedResponse: Response<Unit> = Response(
                status = 201,
                message = "general setting data successfully updated"
            )

            coEvery {
                generalSettingRepository.updateGeneralSetting(body, editBy)
            } returns Unit

            //Act
            val result = generalSettingService.updateGeneralSetting(body, editBy)

            //Assert
            assertEquals(expectedResponse, result)
            assertEquals(201, result.status)
        }
}