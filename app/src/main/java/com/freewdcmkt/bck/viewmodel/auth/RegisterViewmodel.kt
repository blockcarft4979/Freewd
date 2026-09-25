package com.freewdcmkt.bck.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.request.RegisterRequestData
import com.freewdcmkt.bck.data.request.SendAuthCodeRequestData
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.initUserInfo
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import com.freewdcmkt.bck.util.network.toResult
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
            val result = safeApiCall {
                RetroV2Client.apiService.sendAuthCode(SendAuthCodeRequestData(qq))
            }
            when (result
            ) {
                is ApiResult.Success -> {
                    startCountdown()
                    _registerUiState.value = RegisterUiState.SendAuthCodeSuccess
                }

                is ApiResult.Error -> {
                    resetCountdown()
                    _registerUiState.value = RegisterUiState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    resetCountdown()
                    _registerUiState.value = RegisterUiState.Error(isNoNetWork = true)
                }
            }
        }
    }

    fun register(qq: String, password: String, code: String) {
        _registerUiState.value = RegisterUiState.Loading
        viewModelScope.launch {
            val result = safeApiCall {  RetroV2Client.apiService.register(RegisterRequestData(qq, password, code)) }
            when (result) {
                is ApiResult.Success -> {
                    val loginData = result.data
                    if (loginData != null) {
                        initUserInfo(loginData, qq)
                        UserInfoManager.isLoginFlow().first()
                    } else {
                        _registerUiState.value = RegisterUiState.Error("Data Error")
                    }
                }

                is ApiResult.Error -> {
                    _registerUiState.value = RegisterUiState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    _registerUiState.value = RegisterUiState.Error(isNoNetWork = true)
                }
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