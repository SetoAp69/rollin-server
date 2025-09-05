package com.rollinup.server.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    @SerialName("name")
    val name :String = "",
    @SerialName("descriptions")
    val descriptions:String = "",
    @SerialName("priority")
    val priority: Priority = Priority.LOW,
)
