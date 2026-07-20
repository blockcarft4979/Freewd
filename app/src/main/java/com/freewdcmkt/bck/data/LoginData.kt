package com.freewdcmkt.bck.data

import kotlinx.serialization.Serializable

@Serializable
data class LoginData(
    val xp: Int,
    val token: String,
    val username: String,
    val uid : Int,
    val lastCheckInDate: String,
    val checkInDays: Int
)