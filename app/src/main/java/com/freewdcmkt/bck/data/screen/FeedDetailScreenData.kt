package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class FeedDetailScreenData(
    val id: Int, val zone: Int? = null
) {
}

@Serializable
data class FeedDetailData(
    val title: String? = null,
    val msg: String? = null,
    val qq: String,
    val username: String,
    val date: String,
    val likeCount: Int,
    val isLiked: Boolean,
    val isMarkdown: Boolean,
    val img: String? = null,
    val reply: List<FeedReplyData>? = null
)

@Serializable
data class FeedReplyData(
    val commentId: Int,
    val date: String,
    val msg: String,
    val qq: String,
    val username: String
)
