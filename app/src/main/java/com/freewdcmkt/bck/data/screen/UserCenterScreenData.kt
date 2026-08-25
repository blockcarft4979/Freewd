package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class UserCenterScreenData(val uid: Int)

@Serializable
data class UserData(
    val qq: String,
    val username: String,
    val xp: Int,
    val level: Int,
    val joinedAt: String,
    val postCount: Int,
    val totalLikes: Int,
    val checkInDays: Int
)

{
}