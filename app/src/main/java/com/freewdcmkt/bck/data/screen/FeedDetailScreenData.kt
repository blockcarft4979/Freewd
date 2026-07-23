package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class FeedDetailScreenData(
    val id: String
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
    val reply: List<FeedReplyData>
)

@Serializable
data class FeedReplyData(
    val date: String,
    val msg: String,
    val qq: String,
    val username: String
)