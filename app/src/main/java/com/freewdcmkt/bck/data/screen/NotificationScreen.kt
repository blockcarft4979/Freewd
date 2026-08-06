package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
object NotificationScreen
@Serializable
data class NotificationDataList(
    val list: List<NotificationData>
)

@Serializable
data class NotificationData(
    val id: Int,
    val fromQq: String,
    val fromUsername: String,
    val feedId: Int,
    val type: Int,
    val isRead: Boolean,
    val createdAt: String,
    val preview: String? = null,
)