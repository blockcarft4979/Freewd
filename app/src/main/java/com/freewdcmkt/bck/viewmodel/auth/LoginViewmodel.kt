package com.freewdcmkt.bck.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.request.LoginRequestData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.initUserInfo
import com.freewdcmkt.bck.util.network.RetroClient
import com.freewdcmkt.bck.util.network.getErrorMsg
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import retrofit2.HttpException


class LogInViewModel() : ViewModel() {

    private val _loginUiState = MutableStateFlow<LoginUiState>(LoginUiState.NoAction)
    val loginUiState: StateFlow<LoginUiState> = _loginUiState.asStateFlow()
    fun fetchData(password: String, qq: String) {

        viewModelScope.launch {
            _loginUiState.value = LoginUiState.Loading
            val response = RetroClient.apiService.login(LoginRequestData(qq, password))
            try {
                val data = response.body()
                Log.d("LOGIN DATA", data.toString())
                if (data?.data != null) {
                    val loginData = data.data
                    initUserInfo(loginData, qq)
                    _loginUiState.value = LoginUiState.NoAction
                    UserInfoManager.isLoginFlow().first()
                }
                else {
                    val errorData = response.errorBody()?.string() ?: ""
                    val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                    _loginUiState.value = LoginUiState.Error(errorMsg.msg)
                }
            } catch (_: Exception) {
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
