package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.HomeData
import com.freewdcmkt.bck.data.Notification
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class HomeViewmodel : ViewModel() {
    val userAccount = UserInfoManager.getUserAccountFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val username = UserInfoManager.getUsernameFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val uid = UserInfoManager.getUidFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    val homeImageUrl = UserInfoManager.getHomeImageUrlFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )
    private val _homeData = MutableStateFlow(HomeData(Notification(null, null, null), emptyList()))
    val homeData: StateFlow<HomeData> = _homeData.asStateFlow()
    private val _homeUiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val homeUiState: StateFlow<HomeUiState> = _homeUiState.asStateFlow()

    fun fetchData(forceRefresh: Boolean = false) {
        if (!forceRefresh) {
            Log.d("HOME VM", "数据已存在，跳过重复请求")
            return
        }

        viewModelScope.launch {
            try {
                _homeUiState.value = HomeUiState.Loading
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Other.HOME_DATA).build()
                    ).execute()
                }
                val body = response.body.string()
                Log.d("HOME VIEWMODEL", body + response.code)

                if (response.isSuccessful) {
                    _homeUiState.value = HomeUiState.Finish
                    val data = JsonParser.json.decodeFromString<BaseData<HomeData>>(body)
                    Log.d("HOME VIEWMODEL", data.data.toString())
                    if (data.data != null) {
                        _homeData.value = data.data
                        if (data.data.notification.imageUrl != null) UserInfoManager.saveHomeImageUrl(data.data.notification.imageUrl)
                        Log.d("HOME VIEWMODEL", _homeData.value.zone.toString())
                    }
                }
            } catch (e: Exception) {
                _homeUiState.value = HomeUiState.Error(e.message.toString())
                e.printStackTrace()
            }
        }
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    object Finish : HomeUiState()
    class Error(val msg: String) : HomeUiState()

}
