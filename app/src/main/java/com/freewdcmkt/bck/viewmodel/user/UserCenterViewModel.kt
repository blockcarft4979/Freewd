package com.freewdcmkt.bck.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.UserData
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.getSystemDate
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class UserCenterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UserCenterUiState>(UserCenterUiState.Loading)
    val uiState: StateFlow<UserCenterUiState> = _uiState.asStateFlow()

    private val _isShowCheckInDialog = MutableStateFlow(false)
    val isShowChenInDialog: StateFlow<Boolean> = _isShowCheckInDialog.asStateFlow()
    private val _exp = MutableStateFlow(0)
    val exp: StateFlow<Int> = _exp.asStateFlow()

    private val _isChecked = MutableStateFlow(false)
    val isChecked: StateFlow<Boolean> = _isChecked.asStateFlow()

    init {
        viewModelScope.launch {
            UserInfoData.exp.collect { _exp.value = it }
        }

        getUserInfo()
    }
//    init {
//        //getUserInfo()
//        Log.d("User center",UserInfoData.lastCheckInDate.value)
//        Log.d("USER CENTER CURRENT TIME",getSystemDate())
//    }

    fun getUserInfo() {
        _uiState.value = UserCenterUiState.Loading
        viewModelScope.launch {
            try {
                val response = RetroClient.apiService.getUserInfo()
                if (response.data != null) {
                    val data = response.data
                    UserInfoManager.saveCheckInDays(data.checkInDays)
                    UserInfoManager.saveExp(data.xp)
                    UserInfoManager.saveUsername(data.username)
                    _exp.value = data.xp
                    _isChecked.value = true
                    _isShowCheckInDialog.value = false
                    Log.d("USER CENTER", response.data.toString())
                    _uiState.value = UserCenterUiState.Finish
                } else {
                    _uiState.value = UserCenterUiState.LoadError(response.msg)
                }
            } catch (e: Exception) {
                Log.d("USER CENTER",e.message.toString())
                _uiState.value = UserCenterUiState.LoadError(isNoNetwork = true)
            }
        }
    }

    fun checkIn() {
        _isShowCheckInDialog.value = true
        viewModelScope.launch {
          try {
                val response = RetroClient.apiService.checkIn()
              Log.d("USER CENTER rep",response.toString())
                if (response.data != null) {
                    val data = response.data
                    UserInfoManager.saveCheckInDays(data.checkInDays)
                    UserInfoManager.saveExp(data.totalXp)
                    UserInfoManager.saveLastCheckInDate(getSystemDate())
                    _isChecked.value = true
                    _isShowCheckInDialog.value = false
                   // Log.d("USER CENTER", UserInfoData.lastCheckInDate.value)
                    Log.d("USER CENTER ..",data.toString())
                }else{
                  //  _isChecked.value = UserInfoData.lastCheckInDate.value == getSystemDate()
                }
            } catch (e: Exception) {
                _isShowCheckInDialog.value = false
               Log.d("USER CENTER",e.toString())
            }
        }

    }


}

sealed class UserCenterUiState() {
    object Loading : UserCenterUiState()
    object Finish : UserCenterUiState()
    data class LoadError(val msg: String? = null, val isNoNetwork: Boolean = false) :
        UserCenterUiState()
}