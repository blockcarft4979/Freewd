package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class FeedData(
    val page: Int,
    val pages: Int,
    val feed: List<PostsData>
) {
}

@Serializable
data class PostsData(
    val title: String? = null,
    val msg: String? = null,
    val id: Int,
    val username: String,
    val qq: String,
    val date: String,
    @SerialName("like_count")
    val likeCount: Int = 0,
    @SerialName("is_liked")
    val isLiked: Boolean = false,
    val img: String? = null
)