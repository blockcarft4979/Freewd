package com.freewdcmkt.bck.data.screen

import androidx.annotation.Keep
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FeedDetailScreenData(
    val id: Int, val zone: Int? = null
): NavKey {
}

@Serializable
data class FeedDetailData(
    val isError: Boolean? = false,
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

@Serializable
data class ReplyFeedData(
    val id: Int,
    val content: String,
    @SerialName("reply")
    val reply: String? = null
)
@Keep
@Serializable
data class LikeFeedRequestData(val id: Int)
@Serializable
@Keep
data class LikeFeedResultData(
    val isLiked: Boolean,
    val likeCount: Int
)
