package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
object ChangePasswordScreenData : NavKey

@Serializable
data class ChangePasswordRequestData(val qq: String)

@Serializable
data class SubmitPasswordRequestData(
    @SerialName("new_password")
    val password: String,
    val code: String,
    val qq: String
)