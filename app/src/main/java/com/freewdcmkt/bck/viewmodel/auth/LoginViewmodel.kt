package com.freewdcmkt.bck.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class LogInViewModel() : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.NoAction)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()
    fun fetchData(password: String, qq: String) {

        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            try {
                val responseData = RetroClient.apiService.login(LoginRequestData(qq, password))
                Log.d("LOGIN VIEWMODEL", "status=${responseData.status}, msg=${responseData.msg}, data=${responseData.data}")
                if (responseData.data != null) {
                    val loginData = responseData.data
                    TokenManager.saveToken(loginData.token)
                    UserInfoManager.saveUsername(loginData.username)
                    UserInfoManager.saveUid(loginData.uid.toString())
                    UserInfoManager.saveExp(loginData.xp)
                    UserInfoManager.saveCheckInDays(loginData.checkInDays)
                    UserInfoManager.saveLogin(isLogin = true)
                    UserInfoManager.saveUserAccount(qq = qq)
                    _loginUiState.value = LoginUiState.NoAction
                    UserInfoManager.isLoginFlow().first()
                } else {
                    Log.d("VIEW MODEL", "ERROR")
                    _loginUiState.value = LoginUiState.Error(responseData.msg)
                }
            } catch (e: Exception) {
                Log.d("LOGIN Exception",e.toString())
                _loginUiState.value = LoginUiState.Error(isNoNetWork = true)
            }
        }
    }

}


sealed class LoginUiState {
    object NoAction : LoginUiState()
    object Loading : LoginUiState()
    class Error(val msg: String? = null, val isNoNetWork: Boolean = false) : LoginUiState()

}
