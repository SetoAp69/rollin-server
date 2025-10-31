package com.rollinup.server.model.generalsetting

import com.rollinup.server.model.request.generalsetting.EditGeneralSettingBody
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class EditGeneralSettingBodyTest {

    @Test
    fun `instantiation with default values should have all nulls`() {
        val body = EditGeneralSettingBody()

        assertNull(body.semesterStart)
        assertNull(body.semesterEnd)
        assertNull(body.schoolPeriodStart)
        assertNull(body.schoolPeriodEnd)
        assertNull(body.checkInPeriodStart)
        assertNull(body.checkInPeriodEnd)
        assertNull(body.latitude)
        assertNull(body.longitude)
        assertNull(body.radius)
    }

    @Test
    fun `instantiation with all values should hold correct data`() {
        val body = EditGeneralSettingBody(
            semesterStart = 1678886400000L,
            semesterEnd = 1678972800000L,
            schoolPeriodStart = 25200000L,
            schoolPeriodEnd = 54000000L,
            checkInPeriodStart = 23400000L,
            checkInPeriodEnd = 27000000L,
            latitude = -6.2,
            longitude = 106.8,
            radius = 150.0
        )

        assertEquals(1678886400000L, body.semesterStart)
        assertEquals(1678972800000L, body.semesterEnd)
        assertEquals(25200000L, body.schoolPeriodStart)
        assertEquals(54000000L, body.schoolPeriodEnd)
        assertEquals(23400000L, body.checkInPeriodStart)
        assertEquals(27000000L, body.checkInPeriodEnd)
        assertEquals(-6.2, body.latitude)
        assertEquals(106.8, body.longitude)
        assertEquals(150.0, body.radius)
    }

    @Test
    fun `instantiation with partial values should hold correct data`() {
        val body = EditGeneralSettingBody(
            latitude = -7.1,
            radius = 200.0
        )

        assertNull(body.semesterStart)
        assertNull(body.semesterEnd)
        assertEquals(-7.1, body.latitude)
        assertNull(body.longitude)
        assertEquals(200.0, body.radius)
    }
}
