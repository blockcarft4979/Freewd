package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object LoginScreenData: NavKey
@Serializable
data class LoginData(
    val xp: Int? = 0,
    val token: String,
    val username: String,
    val uid: Int,
    val lastCheckInDate: String? = null,
    val checkInDays: Int? = 0
)