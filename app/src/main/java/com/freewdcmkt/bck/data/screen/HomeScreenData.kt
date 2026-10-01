package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data object HomeScreenData: NavKey
@Serializable
data class HomeData(
    val notification: Notification? = null,
)

@Serializable
data class Notification(
    val id: Int,
    val title: String? = null,
    val msg: String? = null,
    val imageUrl: String? = null,
)


@Serializable
data class LikeResult(
    @SerialName("is_liked") val isLiked: Boolean,
    @SerialName("like_count") val likeCount: Int
)

@Serializable
data class VerifyTokenData(val username: String, val unreadCount: Int)

