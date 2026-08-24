package com.freewdcmkt.bck.data.common

import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

object UserInfoData {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    val account = UserInfoManager.getUserAccountFlow()
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    val username = UserInfoManager.getUsernameFlow()
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val uid = UserInfoManager.getUidFlow()
        .stateIn(
            scope = appScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val exp = UserInfoManager.getExpFlow().map { it ?: 0 }.stateIn(
        scope = appScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val checkInDays = UserInfoManager.getCheckInDaysFlow()
        .stateIn(appScope, SharingStarted.WhileSubscribed(5000), initialValue = 0)
}