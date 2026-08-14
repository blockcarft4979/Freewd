package com.freewdcmkt.bck.data

import kotlinx.serialization.Serializable

@Serializable
data class BaseData<T>(
    val status: String,
    val msg: String? = null,
    val data: T? = null,
)

@Serializable
data class ErrorData(
    val status: String,
    val msg: String
)