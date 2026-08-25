package com.freewdcmkt.bck.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.layout.ui.user.MeUiState
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.getSystemDate
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.NoAction)
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()
    private val _isShowSubmittingDialog = MutableStateFlow(false)
    val isShowSubmittingDialog: StateFlow<Boolean> = _isShowSubmittingDialog.asStateFlow()
    private val _isShowCheckInDialog = MutableStateFlow(false)
    val isShowChenInDialog: StateFlow<Boolean> = _isShowCheckInDialog.asStateFlow()

    fun submitUsername(username: String) {
        viewModelScope.launch {
            _isShowSubmittingDialog.value = true
            try {
                val response = RetroClient.apiService.submitUsername(UsernameData(username))
                if (response.data != null) {
                    val data = response.data
                    _isShowSubmittingDialog.value = false
                    UserInfoManager.saveUsername(data.username)
                } else {
                    val errorMsg = response.msg
                    _uiState.value = MeUiState.LoadError(errorMsg)
                    _isShowSubmittingDialog.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = MeUiState.LoadError(isNoNetWork = true)
                _isShowSubmittingDialog.value = false
            }
        }
    }

    fun checkIn() {
        _isShowCheckInDialog.value = true
        viewModelScope.launch {
            try {
                val response = RetroClient.apiService.checkIn()
                Log.d("USER CENTER rep", response.toString())
                if (response.data != null) {
                    val data = response.data
                    UserInfoManager.saveCheckInDays(data.checkInDays)
                    UserInfoManager.saveExp(data.totalXp)
                    UserInfoManager.saveLastCheckInDate(getSystemDate())
                    _isShowCheckInDialog.value = false
                }
            } catch (e: Exception) {
                _isShowCheckInDialog.value = false
                Log.d("USER CENTER", e.toString())
            }
        }

    }


}