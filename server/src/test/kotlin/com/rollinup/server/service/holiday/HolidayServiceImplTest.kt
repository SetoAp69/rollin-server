package com.rollinup.server.service.holiday

import com.rollinup.server.CommonException
import com.rollinup.server.MockkEnvironment
import com.rollinup.server.datasource.database.model.holiday.Holiday
import com.rollinup.server.datasource.database.repository.holiday.HolidayRepository
import com.rollinup.server.mapper.HolidayMapper
import com.rollinup.server.model.request.holiday.CreateHolidayBody
import com.rollinup.server.model.request.holiday.EditHolidayBody
import com.rollinup.server.model.response.Response
import com.rollinup.server.model.response.holiday.GetHolidayListResponse
import com.rollinup.server.util.Utils.toLocalDate
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
import org.junit.Assert.assertEquals
import org.junit.Before
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertFailsWith

class HolidayServiceImplTest {
    private lateinit var holidayService: HolidayService

    private var mapper = HolidayMapper()

    @MockK
    private lateinit var transactionManager: TransactionManager

    @MockK
    private lateinit var holidayRepository: HolidayRepository

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
        MockKAnnotations.init(this)

        envMock.setup()

        arrangeSuspendTransaction()

        holidayService = HolidayServiceImpl(
            transactionManager = transactionManager,
            holidayRepository = holidayRepository,
            mapper = mapper
        )
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `getHolidayList should return correct response`() = runTest {
        //Arrange
        val range = listOf(0L, 12L)
        val localDateRange = range.map { it.toLocalDate() }
        val date = LocalDate.now()
        val expectedList = listOf(
            Holiday(
                id = "asdas",
                name = "holiday",
                date = date
            ),
            Holiday(
                id = "asdas1",
                name = "holiday1",
                date = date
            ),
        )

        val expectedResponse = Response(
            status = 200,
            message = "Success getting holiday data",
            data = GetHolidayListResponse(
                record = 2,
                data = listOf(
                    GetHolidayListResponse.Holiday(
                        id = "asdas",
                        name = "holiday",
                        date = date.toString()
                    ),
                    GetHolidayListResponse.Holiday(
                        id = "asdas1",
                        name = "holiday1",
                        date = date.toString()
                    ),
                )
            )
        )

        coEvery {
            holidayRepository.getHolidayList(localDateRange)
        } returns expectedList

        //Act
        val result = holidayService.getHolidayList(range)

        //Assert
        coVerify {
            holidayRepository.getHolidayList(localDateRange)
        }
        assertEquals(expectedResponse, result)
        assertEquals("Success getting holiday data", result.message)
        assertEquals(200, result.status)
    }


    @Test
    fun `createHoliday should insert new holiday with given value and return correct response`() =
        runTest {
            //Arrange
            val body = CreateHolidayBody(
                name = "my holiday",
                date = 123123L
            )

            val expectedResponse = Response<Unit>(
                status = 201,
                message = "holiday data successfully created",
            )

            coEvery {
                holidayRepository.createHoliday(body)
            } returns Unit

            //Act
            val result = holidayService.createHoliday(body)

            //Assert
            coVerify {
                holidayRepository.createHoliday(body)
            }

            assertEquals(expectedResponse, result)
            assertEquals(201, result.status)
            assertEquals("holiday data successfully created", result.message)
        }


    @Test
    fun `editHoliday should insert new holiday with given value and return correct response`() =
        runTest {
            //Arrange
            val id = "123123"
            val body = EditHolidayBody(
                name = "my holiday",
                date = 123123L
            )
            val date = LocalDate.now()

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "holiday data successfully updated",
            )

            val expectedList = listOf(
                Holiday(
                    id = "123",
                    name = "123",
                    date = LocalDate.now()
                )
            )

            coEvery {
                holidayRepository.getHolidayList(id = id)
            } returns expectedList

            coEvery {
                holidayRepository.editHoliday(id, body)
            } returns Unit

            //Act
            val result = holidayService.editHoliday(id, body)

            //Assert
            coVerify {
                holidayRepository.getHolidayList(id = id)
                holidayRepository.editHoliday(id, body)
            }

            assertEquals(expectedResponse, result)
            assertEquals(202 , result.status)
            assertEquals("holiday data successfully updated", result.message)
        }

    @Test
    fun `editHoliday should throw correct exception when there's no record with given id`() =
        runTest {
            //Arrange
            val id = "123123"
            val body = EditHolidayBody(
                name = "my holiday",
                date = 123123L
            )
            val expectedMessage = "can't find holiday data"

            coEvery {
                holidayRepository.getHolidayList(id = id)
            } returns emptyList()

            //Act
            val result = assertFailsWith<CommonException> { holidayService.editHoliday(id, body) }

            //Assert
            coVerify {
                holidayRepository.getHolidayList(id = id)
            }

            assertEquals(expectedMessage, result.message)
        }

    @Test
    fun `deleteHoliday should delete holiday with given id and return correct response`() =
        runTest {
            //Arrange
            val listId = listOf("123", "122", "124")

            val expectedResponse = Response<Unit>(
                status = 202,
                message = "holiday data successfully deleted"
            )

            coEvery {
                holidayRepository.deleteHoliday(listId)
            } returns Unit

            //Act
            val result = holidayService.deleteHoliday(listId)

            //Assert
            assertEquals(expectedResponse, result)
            assertEquals(202, result.status)
            assertEquals("holiday data successfully deleted", result.message)
        }


}