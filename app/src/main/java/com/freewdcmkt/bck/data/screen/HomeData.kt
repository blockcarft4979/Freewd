package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class HomeData(
    val notification: Notification,
    val zone: List<Zone>
)

@Serializable
data class Notification(
    val title: String? = null,
    val msg: String? = null,
    val imageUrl: String? = null,
)

@Serializable
data class Zone(
    val icon: String,
    val name: String,
    val zone: Int? = null,
    val description: String? = null,
    val msg: String? = null,
    val link: String? = null
)

@Serializable
data class VerifyTokenData(val username: String, val unreadCount: Int)