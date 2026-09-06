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
data class PostFeedRequestData(
    val zone: Int,
    val message: String,
    val title: String?,
    val img: String?
)

@Serializable
data class UploadImgData(
    val url: String
)