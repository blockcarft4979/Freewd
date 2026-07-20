package com.freewdcmkt.bck.data

import kotlinx.serialization.Serializable

@Serializable
data class FeedData(
    val page: Int,
    val pages: Int,
    val feed: List<Feed>
) {
}
@Serializable
data class Feed(
    val title: String,
    val msg: String,
    val id: String,
    val username: String,
    val qq: String,
    val date: String
)