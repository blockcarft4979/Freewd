package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.data.screen.LoginData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.NetworkClient
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody


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
                    UserInfoManager.saveLogin(isLogin = true)
                    UserInfoManager.saveUserAccount(qq = qq)
                    _loginUiState.value = LoginUiState.NoAction
                } else {
                    Log.d("VIEW MODEL", "ERROR")
                    _loginUiState.value = LoginUiState.Error(responseData.msg)
                }
            } catch (e: Exception) {
                Log.d("LOGIN",e.toString())
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
