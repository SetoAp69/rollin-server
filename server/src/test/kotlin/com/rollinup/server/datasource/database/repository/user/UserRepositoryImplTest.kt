//package com.rollinup.server.datasource.database.repository.user
//
//import com.rollinup.server.datasource.database.repository.user.UserRepository
//import com.rollinup.server.datasource.database.model.user.Gender
//import com.rollinup.server.datasource.database.table.RoleTable
//import com.rollinup.server.datasource.database.table.UserTable
//import com.rollinup.server.model.request.user.UserQueryParams
//import com.rollinup.server.model.response.user.UserDTO
//import io.mockk.MockKAnnotations
//import io.mockk.coEvery
//import io.mockk.every
//import io.mockk.impl.annotations.MockK
//import io.mockk.mockk
//import io.mockk.mockkObject
//import io.mockk.unmockkAll
//import kotlinx.coroutines.test.runTest
//import org.jetbrains.exposed.v1.core.ResultRow
//import org.jetbrains.exposed.v1.jdbc.Query
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//import java.util.UUID
//import kotlin.test.assertEquals
//
//class UserRepositoryImplTest {
//
//    private lateinit var userRepositoryASDASD: UserRepositoryASDASD
//
//    @MockK
//    private lateinit var userDao: UserRepository
//
//    val x = mockkObject(UserTable)
//
//
//    private fun arrangeResultRow(
//        id: String,
//        userName: String,
//        email: String,
//        firstName: String,
//        lastName: String,
//        role: String,
//        gender: String,
//        password: String,
//        salt: String
//    ): ResultRow {
//        val row = mockk<ResultRow>()
//        every { row[UserTable.user_id] } returns UUID.fromString(id)
//        every { row[UserTable.username] } returns userName
//        every { row[UserTable.email] } returns email
//        every { row[UserTable.firstName] } returns firstName
//        every { row[UserTable.lastName] } returns lastName
//        every { row[RoleTable.name] } returns role
//        every { row[UserTable.gender] } returns Gender.fromValue(gender)
//        every { row[UserTable.password] } returns password
//        every { row[UserTable.salt] } returns salt
//
//        return row
//    }
//
//    @Before
//    fun setUp() {
//        MockKAnnotations.init(this)
//        userRepositoryASDASD = UserRepositoryASDASDImpl(
//            dao = userDao
//        )
//
//    }
//
//    @After
//    fun tearDown() {
//        unmockkAll()
//    }
//
//    @Test
//    fun `getAllUsers should return list of users`() = runTest {
//        //Arrange
//        val expectedResult = listOf(
//            UserDTO(
//                id = "123e4567-e89b-12d3-a456-426614174000",
//                userName = "userName1",
//                email = "email1",
//                firstName = "firstName1",
//                lastName = "lastName1",
//                role = "role1",
//                gender = "M",
//                password = "password1",
//                salt = "salt1"
//            ),
//            UserDTO(
//                id = "123e4567-e89b-12d3-a456-426614174001",
//                userName = "userName2",
//                email = "email2",
//                firstName = "firstName2",
//                lastName = "lastName2",
//                role = "role2",
//                gender = "F",
//                password = "password2",
//                salt = "salt2"
//            )
//        )
//        val firsRow = arrangeResultRow(
//            id = "123e4567-e89b-12d3-a456-426614174000",
//            userName = "userName1",
//            email = "email1",
//            firstName = "firstName1",
//            lastName = "lastName1",
//            role = "role1",
//            gender = "M",
//            password = "password1",
//            salt = "salt1"
//        )
//
//        val secondRow = arrangeResultRow(
//            id = "123e4567-e89b-12d3-a456-426614174001",
//            userName = "userName2",
//            email = "email2",
//            firstName = "firstName2",
//            lastName = "lastName2",
//            role = "role2",
//            gender = "F",
//            password = "password2",
//            salt = "salt2"
//        )
//
//        val listRow = listOf(firsRow, secondRow)
//        val queryParams = UserQueryParams()
//        val mockkQuery = mockk<Query>()
//
//        coEvery {
//            mockkQuery.iterator()
//        } returns listRow.iterator()
//
//        coEvery {
//            userDao.getAllUsers(queryParams)
//        } returns mockkQuery
//
//        //ACT
//        val result = userRepositoryASDASD.getAllUsers(queryParams)
//
//        //ASSERT
//        assertEquals(
//            expected = expectedResult,
//            actual = result
//        )
//
//    }
//
//
//}