package com.freewdcmkt.bck.data.screen

import androidx.annotation.Keep
import kotlinx.serialization.Serializable



@Serializable
data class UsernameData(val username: String)

@Serializable
@Keep
data class CheckInData(
    val earnedXp: Int? = null,
    val totalXp: Int,
    val checkInDays: Int,
    val lastCheckInDate: String? = null
)