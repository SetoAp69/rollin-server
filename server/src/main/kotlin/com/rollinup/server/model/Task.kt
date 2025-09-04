package com.rollinup.server.model

data class Task(
    val name :String = "",
    val descriptions:String = "",
    val priority: Priority = Priority.LOW,
)
