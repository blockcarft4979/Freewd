package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.LoginData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
                val json = """{"password":"$password","qq":"$qq"}"""
                val requestBody = json.toRequestBody("application/json".toMediaType())
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Auth.LOGIN_URL).post(requestBody).build()
                    )
                        .execute()
                }
                val body = response.body.string()
                Log.d("LOGIN VIEWMODEL", body)
                if (response.isSuccessful) {
                    Log.d("VIEW MODEL", "OK")
                    val data = JsonParser.json.decodeFromString<BaseData<LoginData>>(body)
                    if (data.data != null) {
                        val loginData = data.data
                        TokenManager.saveToken(loginData.token)
                        UserInfoManager.saveUsername(loginData.username)
                        UserInfoManager.saveUid(loginData.uid.toString())
                        UserInfoManager.saveLogin(isLogin = true)
                        UserInfoManager.saveUserAccount(qq = qq)
                        _loginUiState.value = LoginUiState.Success()
                    }
                } else {
                    Log.d("VIEW MODEL", "ERROR")
                    val data = JsonParser.json.decodeFromString<BaseData<Nothing>>(body)
                    if (data.msg != null) {
                        _loginUiState.value = LoginUiState.Error(data.msg)
                        Log.d("LOGIN VIEWMODEL", data.msg)
                    }
                }
            } catch (e: Exception) {
                _loginUiState.value = LoginUiState.Error(e.message.toString())
            }
        }
    }
}


sealed class LoginUiState {
    object NoAction : LoginUiState()
    object Loading : LoginUiState()
    class Success : LoginUiState()
    class Error(val msg: String) : LoginUiState()

}
