package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val xp: Int? = 0,
    val token: String,
    val username: String,
    val uid: Int,
    val lastCheckInDate: String? = null,
    val checkInDays: Int? = 0
)