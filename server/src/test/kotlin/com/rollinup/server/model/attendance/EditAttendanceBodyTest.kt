package com.rollinup.server.model.attendance

import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.model.request.attendance.EditAttendanceBody
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class EditAttendanceBodyTest {

    @Test
    fun `fromHashMap should create body with all fields`() {
        val hashMap = hashMapOf(
            "latitude" to "12.345",
            "longitude" to "67.890",
            "status" to "LATE",
            "checkedInAt" to "1678886400000"
        )

        val expectedBody = EditAttendanceBody(
            location = EditAttendanceBody.Location(
                latitude = 12.345,
                longitude = 67.890
            ),
            status = AttendanceStatus.LATE,
            checkedInAt = 1678886400000L
        )

        val result = EditAttendanceBody.fromHashMap(hashMap)
        assertEquals(expectedBody, result)
    }

    @Test
    fun `fromHashMap should create body with only status`() {
        val hashMap = hashMapOf(
            "status" to "CHECKED_IN"
        )

        val expectedBody = EditAttendanceBody(
            location = EditAttendanceBody.Location(
                latitude = null,
                longitude = null
            ),
            status = AttendanceStatus.CHECKED_IN,
            checkedInAt = null
        )

        val result = EditAttendanceBody.fromHashMap(hashMap)
        assertEquals(expectedBody, result)
    }

    @Test
    fun `fromHashMap should create body with only location`() {
        val hashMap = hashMapOf(
            "latitude" to "1.1",
            "longitude" to "2.2"
        )

        val expectedBody = EditAttendanceBody(
            location = EditAttendanceBody.Location(
                latitude = 1.1,
                longitude = 2.2
            ),
            status = null,
            checkedInAt = null
        )

        val result = EditAttendanceBody.fromHashMap(hashMap)
        assertEquals(expectedBody, result)
    }

    @Test
    fun `fromHashMap should create body with only checkedInAt`() {
        val hashMap = hashMapOf(
            "checkedInAt" to "12345"
        )

        val expectedBody = EditAttendanceBody(
            location = EditAttendanceBody.Location(
                latitude = null,
                longitude = null
            ),
            status = null,
            checkedInAt = 12345L
        )

        val result = EditAttendanceBody.fromHashMap(hashMap)
        assertEquals(expectedBody, result)
    }

    @Test
    fun `fromHashMap should create empty body when hashmap is empty`() {
        val hashMap = hashMapOf<String, String>()

        val expectedBody = EditAttendanceBody(
            location = EditAttendanceBody.Location(
                latitude = null,
                longitude = null
            ),
            status = null,
            checkedInAt = null
        )

        val result = EditAttendanceBody.fromHashMap(hashMap)
        assertEquals(expectedBody, result)
    }

    @Test
    fun `fromHashMap should have null fields for invalid formats`() {
        val hashMap = hashMapOf(
            "latitude" to "not-a-double",
            "longitude" to "also-not-a-double",
            "checkedInAt" to "not-a-long"
        )

        val result = EditAttendanceBody.fromHashMap(hashMap)

        assertNull(result.location.latitude)
        assertNull(result.location.longitude)
        assertNull(result.checkedInAt)
    }

}
