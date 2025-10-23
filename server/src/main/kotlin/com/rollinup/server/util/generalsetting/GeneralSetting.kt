package com.rollinup.server.util.generalsetting

import java.time.OffsetTime

data class GeneralSetting(
    val semesterStart: OffsetTime,
    val semesterEnd: OffsetTime,
    val checkInPeriodStart: OffsetTime,
    val checkInPeriodEnd: OffsetTime,
    val schoolPeriodStart: OffsetTime,
    val schoolPeriodEnd: OffsetTime
)
