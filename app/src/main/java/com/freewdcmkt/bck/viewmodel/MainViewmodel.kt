package com.freewdcmkt.bck.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewmodel : ViewModel() {
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Loading)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    init {
        viewModelScope.launch {
            UserInfoManager.isLoginFlow().collect { isLogin ->
                _loginState.value =
                    if (isLogin && TokenManager.getToken() != null) LoginState.LoggedIn else LoginState.LoggedOut
            }
        }
    }
}

// MainViewmodel.kt
sealed class LoginState {
    object Loading : LoginState()
    object LoggedIn : LoginState()
    object LoggedOut : LoginState()
}