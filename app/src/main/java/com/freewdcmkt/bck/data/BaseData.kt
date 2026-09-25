package com.freewdcmkt.bck.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BaseData<T>(
    val status: String,
    val msg: String? = null,
    val data: T? = null,
)

@Serializable
data class ApiResponse<T>(
    @SerialName("success")
    val success: Boolean? = false,
    @SerialName("data")
    val data: T? = null,
    @SerialName("message")
    val message: String? = null,
) {
    val isSuccess: Boolean
        get() = success == true

    val errorMessage: String
        get() = message ?: "Unknown Error:("
}