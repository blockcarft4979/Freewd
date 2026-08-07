package com.freewdcmkt.bck.api

object Link {
    private const val FEED_LINK = "https://community.freewd.top/u/page?id="
    fun feedLink(id: Int) = "$FEED_LINK$id"
}