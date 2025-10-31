package com.rollinup.server.model.attendance

import com.rollinup.server.model.request.attendance.GetAttendanceByStudentQueryParams
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetAttendanceByStudentQueryParamsTest {

    @Test
    fun `instantiation with default values should have all nulls`() {
        val queryParams = GetAttendanceByStudentQueryParams()

        assertNull(queryParams.search)
        assertNull(queryParams.limit)
        assertNull(queryParams.page)
        assertNull(queryParams.dateRange)
    }

    @Test
    fun `instantiation with all values should hold correct data`() {
        val dateRange = listOf(1678886400000L, 1678972800000L)
        val queryParams = GetAttendanceByStudentQueryParams(
            search = "Jane",
            limit = 50,
            page = 3,
            dateRange = dateRange
        )

        assertEquals("Jane", queryParams.search)
        assertEquals(50, queryParams.limit)
        assertEquals(3, queryParams.page)
        assertEquals(dateRange, queryParams.dateRange)
    }

    @Test
    fun `instantiation with blank search should hold correct data`() {
        val queryParams = GetAttendanceByStudentQueryParams(
            search = "",
            limit = 10,
            page = 1,
            dateRange = emptyList()
        )

        assertEquals("", queryParams.search)
        assertEquals(10, queryParams.limit)
        assertEquals(1, queryParams.page)
        assertEquals(emptyList(), queryParams.dateRange)
    }
}
