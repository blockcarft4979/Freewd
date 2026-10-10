package com.freewdcmkt.bck.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import com.freewdcmkt.bck.util.time.getSystemDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeViewModel : ViewModel() {
    private val _errorMsg = MutableStateFlow("")
    val errorMsg: StateFlow<String> = _errorMsg.asStateFlow()
    private val _isShowSubmittingDialog = MutableStateFlow(false)
    val isShowSubmittingDialog: StateFlow<Boolean> = _isShowSubmittingDialog.asStateFlow()
    private val _isShowCheckInDialog = MutableStateFlow(false)
    val isShowChenInDialog: StateFlow<Boolean> = _isShowCheckInDialog.asStateFlow()

    fun submitUsername(username: String) {
        viewModelScope.launch {
            _isShowSubmittingDialog.value = true
            when (val result = safeApiCall { RetroV2Client.apiService.submitUsername(UsernameData(username)) }) {
                is ApiResult.Success -> {
                    result.data?.username?.let { UserInfoManager.saveUsername(it) }
                    _isShowSubmittingDialog.value = false
                }

                is ApiResult.Error -> {
                    _errorMsg.value = result.message
                    _isShowSubmittingDialog.value = false
                }

                is ApiResult.NetworkError -> {
                    _isShowSubmittingDialog.value = false
                    Log.d("USER CENTER", "submitUsername network error")
                }
            }
        }
    }

    fun checkIn() {
        _isShowCheckInDialog.value = true
        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.checkIn() }) {
                is ApiResult.Success -> {
                    result.data?.let { data ->
                        UserInfoManager.saveCheckInDays(data.checkInDays)
                        UserInfoManager.saveExp(data.totalXp)
                        UserInfoManager.saveLastCheckInDate(data.lastCheckInDate ?: getSystemDate())
                    }
                    _isShowCheckInDialog.value = false
                }

                is ApiResult.Error -> {
                    _errorMsg.value = result.message
                    _isShowCheckInDialog.value = false
                }

                is ApiResult.NetworkError -> {
                    _isShowCheckInDialog.value = false
                    Log.d("USER CENTER", "checkIn network error")
                }
            }
        }
    }


}
