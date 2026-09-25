package com.freewdcmkt.bck.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.screen.ChangePasswordRequestData
import com.freewdcmkt.bck.data.screen.SubmitPasswordRequestData
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel : ViewModel() {

    private val _countdown = MutableStateFlow(0)
    val countdown: StateFlow<Int> = _countdown.asStateFlow()

    private var countdownJob: Job? = null
    private val _uiState = MutableStateFlow<ChangePasswordUiState>(ChangePasswordUiState.NoAction)
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

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

    fun sendCode(qq: String) {
        _uiState.value = ChangePasswordUiState.Loading
        viewModelScope.launch {
            val result =
                safeApiCall { RetroV2Client.apiService.sendResetCode(ChangePasswordRequestData(qq)) }
            when (result) {
                is ApiResult.Success -> {

                    startCountdown()
                    _uiState.value = ChangePasswordUiState.OnSendCodeSuccess
                }

                is ApiResult.Error -> {
                    resetCountdown()
                    _uiState.value = ChangePasswordUiState.Error(errorMsg = result.message)
                }

                is ApiResult.NetworkError -> {
                    resetCountdown()
                    _uiState.value = ChangePasswordUiState.Error(isNoNetWork = true)
                }

            }
        }
    }

    fun submitPassword(qq: String, password: String, code: String) {
        _uiState.value = ChangePasswordUiState.Loading
        viewModelScope.launch {
            val result = safeApiCall {
                RetroV2Client.apiService.submitPassword(
                    SubmitPasswordRequestData(
                        qq = qq,
                        password = password,
                        code = code
                    )
                )
            }
            when (result) {
                is ApiResult.Success -> {
                    _uiState.value = ChangePasswordUiState.OnChangedPassword
                    TokenManager.clearToken()
                    UserInfoManager.clearAllData()
                }

                is ApiResult.Error -> {
                    _uiState.value = ChangePasswordUiState.Error(errorMsg = result.message)
                }

                is ApiResult.NetworkError -> {
                    _uiState.value = ChangePasswordUiState.Error(isNoNetWork = true)
                }
            }
        }
    }

}

sealed class ChangePasswordUiState() {
    object NoAction : ChangePasswordUiState()

    object Loading : ChangePasswordUiState()
    object OnSendCodeSuccess : ChangePasswordUiState()
    object OnChangedPassword : ChangePasswordUiState()
    class Error(val errorMsg: String? = null, val isNoNetWork: Boolean = false) :
        ChangePasswordUiState()
}