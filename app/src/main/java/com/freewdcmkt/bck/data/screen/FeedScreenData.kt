package com.freewdcmkt.bck.data.screen

import kotlinx.serialization.Serializable

@Serializable
data class FeedScreenData(
    val zone: Int
)

@Serializable
data class FeedData(
    val page: Int,
    val pages: Int,
    val feed: List<Feed>
) {
}

@Serializable
data class Feed(
    val title: String? = null,
    val msg: String? = null,
    val id: Int,
    val username: String,
    val qq: String,
    val date: String,
    val img: String? = null
)