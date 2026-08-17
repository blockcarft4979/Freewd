package com.freewdcmkt.bck.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.MeData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class UserCenterViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<UserCenterUiState>(UserCenterUiState.Loading)
    val uiState: StateFlow<UserCenterUiState> = _uiState.asStateFlow()
    private var isLoadedMeData: Boolean = false
    fun getUserInfo() {
        if (isLoadedMeData) {
            return
        }
        _uiState.value = UserCenterUiState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.User.GET_USER_INFO_URL).build()
                    ).execute()
                }
                val body = response.body.string()
                val data = JsonParser.json.decodeFromString<BaseData<MeData>>(body)
                if (response.isSuccessful && data.data != null) {
                    _uiState.value = UserCenterUiState.Finish(data.data)
                    isLoadedMeData = true
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _uiState.value = UserCenterUiState.LoadError(errorData.msg)
                }
            } catch (e: Exception) {
                _uiState.value = UserCenterUiState.LoadError(  isNoNetwork = true)
            }
        }
    }

}

sealed class UserCenterUiState() {
    object Loading : UserCenterUiState()
    data class Finish(val data: MeData) : UserCenterUiState()
    data class LoadError(val msg: String? = null, val isNoNetwork: Boolean = false) :
        UserCenterUiState()
}