package com.rollinup.server.route.test

import com.rollinup.server.util.Utils
import com.rollinup.server.util.Utils.getOffset
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId

fun main() {
//    val offset = OffsetDateTime.now().offset
//    val currentDate =
//        LocalDateTime
//            .of(LocalDate.now(), LocalTime.of(0, 0))
//            .toInstant(offset)
//            .toEpochMilli()
//
//    val instantB = Instant.now().plusSeconds(7200)
//    val instantA = Instant.now()
//    val duration = Duration.between(instantA,instantB).toHours()
//    println(duration)


    val start = Instant.ofEpochMilli(1760854256108)
    val end =  Instant.ofEpochMilli(1760854270432 )
}