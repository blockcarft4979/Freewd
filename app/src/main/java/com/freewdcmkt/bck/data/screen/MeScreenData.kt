package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data object MeScreenData {
}

@Serializable
data class MeData(
    val xp: Int,
    val level: Int,
    val postCount: Int,
    val totalLikes: Int
)

@Serializable
data class UsernameData(val username: String)