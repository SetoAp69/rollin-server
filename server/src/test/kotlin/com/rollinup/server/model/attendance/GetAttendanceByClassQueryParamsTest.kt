package com.rollinup.server.model.attendance

import com.rollinup.server.model.request.attendance.GetAttendanceByClassQueryParams
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetAttendanceByClassQueryParamsTest {

    @Test
    fun `instantiation with default values should have all nulls`() {
        val queryParams = GetAttendanceByClassQueryParams()

        assertNull(queryParams.limit)
        assertNull(queryParams.page)
        assertNull(queryParams.sortBy)
        assertNull(queryParams.order)
        assertNull(queryParams.search)
        assertNull(queryParams.status)
        assertNull(queryParams.date)
    }

    @Test
    fun `instantiation with all values should hold correct data`() {
        val statusList = listOf("LATE", "CHECKED_IN")
        val date = 1678886400000L

        val queryParams = GetAttendanceByClassQueryParams(
            limit = 10,
            page = 1,
            sortBy = "name",
            order = "asc",
            search = "John Doe",
            status = statusList,
            date = date
        )

        assertEquals(10, queryParams.limit)
        assertEquals(1, queryParams.page)
        assertEquals("name", queryParams.sortBy)
        assertEquals("asc", queryParams.order)
        assertEquals("John Doe", queryParams.search)
        assertEquals(statusList, queryParams.status)
        assertEquals(date, queryParams.date)
    }

    @Test
    fun `instantiation with some blank values should hold correct data`() {
        val queryParams = GetAttendanceByClassQueryParams(
            limit = 25,
            page = 2,
            search = "", // Blank search
            status = emptyList() // Empty list
        )

        assertEquals(25, queryParams.limit)
        assertEquals(2, queryParams.page)
        assertEquals("", queryParams.search)
        assertEquals(emptyList(), queryParams.status)
        assertNull(queryParams.sortBy)
        assertNull(queryParams.order)
        assertNull(queryParams.date)
    }
}
