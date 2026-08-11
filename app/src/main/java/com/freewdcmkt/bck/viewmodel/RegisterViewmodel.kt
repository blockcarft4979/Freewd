package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.LoginData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

class RegisterViewmodel : ViewModel() {
    private val _countdown = MutableStateFlow(0) // 剩余秒数，0 表示可用
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var countdownJob: Job? = null

    fun startCountdown(seconds: Int = 60) {
        countdownJob?.cancel()
        _countdown.value = seconds
        countdownJob = viewModelScope.launch {
            while (_countdown.value > 0) {
                delay(1000L)
                _countdown.value -= 1
            }
        }
    }

    fun resetCountdown() {
        countdownJob?.cancel()
        _countdown.value = 0
    }

    private val _registerUiState = MutableStateFlow<RegisterUiState>(RegisterUiState.NoAction)
    val registerUiState: StateFlow<RegisterUiState> = _registerUiState.asStateFlow()

    fun sendCode(qq: String) {
        _registerUiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            try {
                val requestBody = buildJsonObject { put("qq", qq) }.toString()
                    .toRequestBody("application/json".toMediaType())
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Auth.SEND_AUTH_CODE_URL).post(requestBody)
                            .build()
                    ).execute()
                }
                val body = response.body.string()
                Log.d("REGISTER VIEWMODEL", body + response.code)
                if (response.isSuccessful) {
                    startCountdown()
                    _registerUiState.value = RegisterUiState.SendAuthCodeSuccess
                } else {
                    resetCountdown()
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _registerUiState.value = RegisterUiState.Error(errorData.msg)
                }
            } catch (e: Exception) {
                resetCountdown()
                _registerUiState.value = RegisterUiState.Error(isNoNetWork = true)
            }
        }
    }

    fun register(qq: String, password: String, code: String) {
        _registerUiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            try {
                val requestBody =
                    buildJsonObject {
                        put("qq", qq)
                        put("password", password)
                        put("code", code)
                    }.toString().toRequestBody("application/json".toMediaType())
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Auth.REGISTER_URL).post(requestBody)
                            .build()
                    ).execute()
                }
                val body = response.body.string()
                val data = JsonParser.json.decodeFromString<BaseData<LoginData>>(body)
                if (response.isSuccessful && data.data != null) {

                    val loginData = data.data
                    TokenManager.saveToken(loginData.token)
                    UserInfoManager.saveUsername(loginData.username)
                    UserInfoManager.saveUid(loginData.uid.toString())
                    UserInfoManager.saveLogin(isLogin = true)
                    UserInfoManager.saveUserAccount(qq = qq)
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _registerUiState.value = RegisterUiState.Error(errorData.msg)
                }
            } catch (e: Exception) {
                _registerUiState.value = RegisterUiState.Error(isNoNetWork = true)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
    }
}

sealed class RegisterUiState() {
    object NoAction : RegisterUiState()
    object SendAuthCodeSuccess : RegisterUiState()
    object Loading : RegisterUiState()
    class Error(val msg: String? = null, val isNoNetWork: Boolean = false) : RegisterUiState()
}