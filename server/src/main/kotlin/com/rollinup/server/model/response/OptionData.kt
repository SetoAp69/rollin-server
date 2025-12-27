package com.rollinup.server.model.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OptionData<T>(
    @SerialName("label")
    val label: String,
    @SerialName("value")
    val value: T,
)
