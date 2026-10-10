package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class UserProfileScreen(val qq: String) : NavKey

/** 个人中心（看自己的主页，额外带等级经验卡） */
@Serializable
data object UserCenterScreen : NavKey

/**
 * GET /users/{qq} 的返回（用户资料）。
 *
 * 服务端字段是 snake_case（joined_at / post_count / total_likes / check_in_days /
 * last_checkin_date），JsonParser 配了 SnakeCase 命名策略，所以属性用驼峰即可。
 */
@Serializable
data class UserProfileData(
    val uid: Int = 0,
    val qq: String = "",
    val username: String = "",
    val xp: Int = 0,
    val joinedAt: String? = null,
    val postCount: Int = 0,
    val totalLikes: Int = 0,
    val checkInDays: Int = 0,
    val lastCheckinDate: String? = null,
    val unreadCount: Int = 0
)
