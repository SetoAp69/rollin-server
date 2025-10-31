package com.rollinup.server.route.test

import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.getOffset
import com.rollinup.server.util.Utils.toLocalDateTime
import java.time.Instant
import java.time.LocalTime
import java.time.OffsetDateTime

fun main() {
//    val timeS = "2025-10-28T05:00:13.610Z"
//    val localTime = timeS.toLocalDateTime().atZone(getOffset()).toLocalTime()
//    val localTime2 = timeS.toLocalDateTime().atOffset(getOffset()).toLocalTime()
//    val localTime3 = Instant.parse(timeS).let {
//        LocalTime.ofInstant(it, getOffset())
//    }
//
//
//    println(localTime)
//    println(localTime2)
//    println(localTime3)

    val string = "2025-11-04T16:00Z"

    val localTime1 = OffsetDateTime.parse(string).toInstant().let { LocalTime.ofInstant(it, Utils.getOffset()) }
    println(localTime1)
}