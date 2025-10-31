package com.rollinup.server.model.attendance

import com.rollinup.server.CommonException
import com.rollinup.server.datasource.database.model.AttendanceStatus
import com.rollinup.server.model.request.attendance.CreateAttendanceBody
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CreateAttendanceBodyTest {

    @Test
    fun `fromHashMap should create body successfully with all fields`() {
        val hashMap = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            "longitude" to "67.890",
            "status" to "LATE",
            "checkedInAt" to "1678886400000"
        )

        val expectedBody = CreateAttendanceBody(
            studentUserId = "student123",
            latitude = 12.345,
            longitude = 67.890,
            status = AttendanceStatus.LATE,
            checkedInAt = 1678886400000L
        )

        val result = CreateAttendanceBody.fromHashMap(hashMap)
        assertEquals(expectedBody, result)
    }

    @Test
    fun `fromHashMap should use default CHECKED_IN status when status is null or blank`() {
        val hashMap = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            "longitude" to "67.890",
            "checkedInAt" to "1678886400000"
            // "status" is missing
        )

        val result = CreateAttendanceBody.fromHashMap(hashMap)
        assertEquals(AttendanceStatus.CHECKED_IN, result.status)

        val hashMapBlank = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            "longitude" to "67.890",
            "status" to "", // Blank status
            "checkedInAt" to "1678886400000"
        )

        val resultBlank = CreateAttendanceBody.fromHashMap(hashMapBlank)
        assertEquals(AttendanceStatus.CHECKED_IN, resultBlank.status)
    }

    @Test
    fun `fromHashMap should throw exception when studentUserId is null or blank`() {
        val hashMap = hashMapOf(
            "latitude" to "12.345",
            "longitude" to "67.890",
            "checkedInAt" to "1678886400000"
        )

        val ex = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMap)
        }
        assertEquals("Id cannot be empty", ex.message)

        val hashMapBlank = hashMapOf(
            "studentUserId" to "",
            "latitude" to "12.345",
            "longitude" to "67.890",
            "checkedInAt" to "1678886400000"
        )

        val exBlank = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMapBlank)
        }
        assertEquals("Id cannot be empty", exBlank.message)
    }

    @Test
    fun `fromHashMap should throw exception when latitude is invalid or missing`() {
        val hashMap = hashMapOf(
            "studentUserId" to "student123",
            // "latitude" is missing
            "longitude" to "67.890",
            "checkedInAt" to "1678886400000"
        )

        val ex = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMap)
        }
        assertEquals("Location is invalid", ex.message)

        val hashMapInvalid = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "not-a-double",
            "longitude" to "67.890",
            "checkedInAt" to "1678886400000"
        )

        val exInvalid = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMapInvalid)
        }
        assertEquals("Location is invalid", exInvalid.message)
    }

    @Test
    fun `fromHashMap should throw exception when longitude is invalid or missing`() {
        val hashMap = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            // "longitude" is missing
            "checkedInAt" to "1678886400000"
        )

        val ex = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMap)
        }
        assertEquals("Location is invalid", ex.message)

        val hashMapInvalid = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            "longitude" to "not-a-double",
            "checkedInAt" to "1678886400000"
        )

        val exInvalid = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMapInvalid)
        }
        assertEquals("Location is invalid", exInvalid.message)
    }

    @Test
    fun `fromHashMap should throw exception when checkedInAt is invalid or missing`() {
        val hashMap = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            "longitude" to "67.890"
            // "checkedInAt" is missing
        )

        val ex = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMap)
        }
        assertEquals("Check in time is invalid", ex.message)

        val hashMapInvalid = hashMapOf(
            "studentUserId" to "student123",
            "latitude" to "12.345",
            "longitude" to "67.890",
            "checkedInAt" to "not-a-long"
        )

        val exInvalid = assertFailsWith<CommonException> {
            CreateAttendanceBody.fromHashMap(hashMapInvalid)
        }
        assertEquals("Check in time is invalid", exInvalid.message)
    }

}
