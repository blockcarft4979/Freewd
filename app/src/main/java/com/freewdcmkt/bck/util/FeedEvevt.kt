package com.freewdcmkt.bck.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object FeedEvent {
    private val _refreshEvents = MutableStateFlow(0)
    val refreshEvents = _refreshEvents.asStateFlow()

    fun emitRefresh() {
        _refreshEvents.value += 1
    }
}