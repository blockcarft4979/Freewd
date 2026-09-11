package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostFeedScreen(val id: Int? = null, val zone: Int) : NavKey

@Serializable
data class PostFeedData(
    val id: String,
    val xp: Int? = null,
)

@Serializable
data class PostFeedRequestData(
    val zone: Int,
    @SerialName("is_anonymous")
    val isAnonymous: Boolean? = false,
    val message: String,
    val title: String?,
    val img: String?
)

@Serializable
data class UploadImgData(
    val url: String
)