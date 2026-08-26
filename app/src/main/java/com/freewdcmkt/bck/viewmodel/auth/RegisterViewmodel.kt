package com.freewdcmkt.bck.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.request.RegisterRequestData
import com.freewdcmkt.bck.data.request.SendAuthCodeRequestData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.NetworkClient
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.encodeToJsonElement
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class RegisterViewmodel : ViewModel() {
    private val _countdown = MutableStateFlow(0)
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
                val requestBody = JsonParser.json.encodeToJsonElement( SendAuthCodeRequestData(qq)).toString().toRequestBody("application/json".toMediaType())

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
                val response =
                    RetroClient.apiService.register(RegisterRequestData(qq, password, code))

                if (response.data != null) {

                    val loginData = response.data
                    TokenManager.saveToken(loginData.token)
                    UserInfoManager.saveUsername(loginData.username)
                    UserInfoManager.saveUid(loginData.uid.toString())
                    UserInfoManager.saveExp(loginData.xp)
                    UserInfoManager.saveCheckInDays(loginData.checkInDays)
                    UserInfoManager.saveLastCheckInDate(loginData.lastCheckInDate ?: "")
                    UserInfoManager.saveLogin(isLogin = true)
                    UserInfoManager.saveUserAccount(qq = qq)
                    UserInfoManager.isLoginFlow().first()
                } else if (response.msg != null) {
                    _registerUiState.value = RegisterUiState.Error(response.msg)
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