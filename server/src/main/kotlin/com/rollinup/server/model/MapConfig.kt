package com.rollinup.server.model

data class MapConfig(
    val initialLat: Double = 0.0,
    val initialLong: Double = 0.0,
    val initialRad: Double = 0.0,
    val apiKey: String = "",
)
