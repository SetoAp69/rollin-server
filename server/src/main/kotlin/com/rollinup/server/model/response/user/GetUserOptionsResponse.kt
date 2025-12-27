package com.rollinup.server.model.response.user

import com.rollinup.server.model.response.OptionData
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetUserOptionsResponse(
    @SerialName("roles")
    val roles: List<OptionData<Int>> = emptyList(),
    @SerialName("class")
    val classX: List<OptionData<Int>> = emptyList(),
    @SerialName("rolesId")
    val rolesId: List<OptionData<String>> = emptyList(),
    @SerialName("classId")
    val classId: List<OptionData<String>> = emptyList(),
)
