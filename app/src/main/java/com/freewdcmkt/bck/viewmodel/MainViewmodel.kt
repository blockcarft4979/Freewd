package com.freewdcmkt.bck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class MainViewmodel : ViewModel(){
    val isLogin = UserInfoManager.isLoginFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(0),
            initialValue = false
        )
}