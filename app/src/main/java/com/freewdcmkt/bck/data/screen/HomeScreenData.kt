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

/**
 * v2 GET /auth/verify 的返回。
 * 服务端字段是 snake_case（unread_count），JsonParser 配了 SnakeCase 命名策略，无需注解。
 */
@Serializable
data class VerifyTokenData(
    val username: String,
    val unreadCount: Int = 0,
    val qq: String? = null
)

