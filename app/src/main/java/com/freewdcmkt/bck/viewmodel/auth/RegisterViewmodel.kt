package com.freewdcmkt.bck.viewmodel.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.request.RegisterRequestData
import com.freewdcmkt.bck.data.request.SendAuthCodeRequestData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.initUserInfo
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

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
                val response = RetroClient.apiService.sendAuthCode(SendAuthCodeRequestData(qq))

                if (response.isSuccessful) {
                    startCountdown()
                    _registerUiState.value = RegisterUiState.SendAuthCodeSuccess
                } else {
                    val errorData = response.errorBody()?.string() ?: ""
                    val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                    resetCountdown()
                    _registerUiState.value = RegisterUiState.Error(errorMsg.msg)
                }
            } catch (e: Exception) {
                Log.d("SEND CODE ERROR",e.message.toString())
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
                val data = response.body()
                if (data?.data != null) {
                    val loginData = data.data
                    initUserInfo(loginData, qq)
                    UserInfoManager.isLoginFlow().first()
                } else {
                    val errorData = response.errorBody()?.string() ?: ""
                    val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                    _registerUiState.value = RegisterUiState.Error(errorMsg.msg)
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