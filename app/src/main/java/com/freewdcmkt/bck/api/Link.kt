package com.freewdcmkt.bck.api

object Link {
    private const val FEED_LINK = "https://bbs.freewd.top/#/post/"
    fun feedLink(id: Int) = "$FEED_LINK$id"
    const val GITHUB_LINK = "https://github.com/blockcarft4979"
}