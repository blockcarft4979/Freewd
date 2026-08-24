package com.freewdcmkt.bck.data.request

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestData(val qq: String, val password: String, val code: String) {
}
@Serializable
data class SendAuthCodeRequestData(val qq: String)
