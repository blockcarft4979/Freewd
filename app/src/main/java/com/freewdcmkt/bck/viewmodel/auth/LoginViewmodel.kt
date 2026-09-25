package com.freewdcmkt.bck.viewmodel.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.initUserInfo
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class LogInViewModel() : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.NoAction)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()

    fun fetchData(password: String, qq: String) {
        _loginUiState.value = LoginUiState.Loading
        viewModelScope.launch {
            val result = safeApiCall {
                RetroV2Client.apiService.login(LoginRequestData(qq, password))
            }
            when (result) {
                is ApiResult.Success -> {
                    val loginData = result.data
                    if (loginData != null) {
                        initUserInfo(loginData, qq)
                        _loginUiState.value = LoginUiState.NoAction
                        UserInfoManager.isLoginFlow().first()
                    } else {
                        _loginUiState.value = LoginUiState.Error("Data Error")
                    }
                }

                is ApiResult.Error -> {
                    _loginUiState.value = LoginUiState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    _loginUiState.value = LoginUiState.Error(isNoNetWork = true)
                }
            }
        }
    }

}


sealed class LoginUiState {
    object NoAction : LoginUiState()
    object Loading : LoginUiState()
    class Error(val msg: String? = null, val isNoNetWork: Boolean = false) : LoginUiState()

}
