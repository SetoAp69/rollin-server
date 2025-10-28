package com.rollinup.server.route.test

import com.rollinup.server.util.Utils.getOffset
import com.rollinup.server.util.Utils.toLocalDateTime
import java.time.Instant
import java.time.LocalTime

fun main() {
    val timeS = "2025-10-28T05:00:13.610Z"
    val localTime = timeS.toLocalDateTime().atZone(getOffset()).toLocalTime()
    val localTime2 = timeS.toLocalDateTime().atOffset(getOffset()).toLocalTime()
    val localTime3 = Instant.parse(timeS).let {
        LocalTime.ofInstant(it, getOffset())
    }


    println(localTime)
    println(localTime2)
    println(localTime3)
}