package com.freewdcmkt.bck.data.common

import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.time.getSystemDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    val exp = UserInfoManager.getExpFlow().map { it }.stateIn(
        scope = appScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )
    val checkInDays = UserInfoManager.getCheckInDaysFlow()
        .stateIn(appScope, SharingStarted.WhileSubscribed(5000), initialValue = 0)
    private val lastCheckInDate = UserInfoManager.getLastCheckInDate()
        .stateIn(appScope, SharingStarted.WhileSubscribed(5000), initialValue = "")
    private val _isChecked = MutableStateFlow(false)

    val isChecked: StateFlow<Boolean> = _isChecked.asStateFlow()

    init {
        appScope.launch {
            lastCheckInDate.collect { date ->
                _isChecked.value = date == getSystemDate()
            }
        }
    }

    private val _unreadNotificationCount = MutableStateFlow(0)
    val unreadNotificationCount: StateFlow<Int> = _unreadNotificationCount.asStateFlow()
    fun updateUnreadCount(count: Int) {
        _unreadNotificationCount.value = count
    }

    fun clearUnreadCount() {
        _unreadNotificationCount.value = 0
    }
    fun refreshCheckStatus() {
        val savedDate = lastCheckInDate.value
        _isChecked.value = savedDate == getSystemDate()
    }
}