package com.freewdcmkt.bck.data.request

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestData(
    @SerialName("qq")
    val account: String,
    val password: String
) {
}