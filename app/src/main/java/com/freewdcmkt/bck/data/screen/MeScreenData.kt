package com.freewdcmkt.bck.data.screen

import androidx.annotation.Keep
import kotlinx.serialization.Serializable



/**
 * POST /users/me/username 的请求体与返回。
 * 返回里带 uid 时正好用于刷新本地 uid，服务端不返回也不会解析失败（默认 null）。
 */
@Serializable
data class UsernameData(
    val username: String,
    val uid: Int? = null
)

@Serializable
@Keep
data class CheckInData(
    val earnedXp: Int? = null,
    val totalXp: Int,
    val checkInDays: Int,
    val lastCheckInDate: String? = null,
    val alreadyCheckedIn: Boolean = false
)