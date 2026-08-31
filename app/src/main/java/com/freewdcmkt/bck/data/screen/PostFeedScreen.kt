package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class PostFeedScreen(val id: Int? = null, val zone: Int)

@Serializable
data class PostFeedData(
    val id: String,
    val xp: Int? = null,
)

@Serializable
data class UploadImgData(val url: String)