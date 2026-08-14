package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.data.screen.Notification
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.data.screen.VerifyTokenData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.network.NetworkClient
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

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
    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.NoAction)
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()


    private val _verifyTokenData = MutableStateFlow(VerifyTokenData("", 0))
    val verifyTokenData: StateFlow<VerifyTokenData> = _verifyTokenData.asStateFlow()

    fun fetchData(forceRefresh: Boolean = false) {
        Log.d("HOME VM", "是否刷新$forceRefresh")
        if (!forceRefresh) {
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
                        if (data.data.notification.imageUrl != null) UserInfoManager.saveHomeImageUrl(
                            data.data.notification.imageUrl
                        )
                        Log.d("HOME VIEWMODEL", _homeData.value.zone.toString())
                    }
                } else {
                    _homeUiState.value = HomeUiState.Error(null, true)
                }
            } catch (e: Exception) {
                _homeUiState.value = HomeUiState.Error(e.message.toString(), true)
                e.printStackTrace()
            }
        }
    }


    fun submitUsername(username: String) {
        val requestBody = buildJsonObject { put("username", username) }.toString()
            .toRequestBody("application/json".toMediaType())
        viewModelScope.launch {
            _uiState.value = MeUiState.SubmittingUsername
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.User.SUBMIT_USER_NAME_URL)
                            .post(requestBody)
                            .build()
                    ).execute()
                }
                val body = response.body.string()
                val data = JsonParser.json.decodeFromString<BaseData<UsernameData>>(body)
                if (response.isSuccessful && data.data != null) {
                    _uiState.value = MeUiState.SubmitFinish
                    UserInfoManager.saveUsername(data.data.username)
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _uiState.value = MeUiState.LoadError(errorData.msg)
                }
            } catch (e: Exception) {
                _uiState.value = MeUiState.LoadError(isNoNetWork = true)
            }
        }
    }

    fun verifyToken() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Auth.VERIFY_TOKEN_URL).build()
                    ).execute()
                }
                val body = response.body.string()
                val data = JsonParser.json.decodeFromString<BaseData<VerifyTokenData>>(body)
                if (response.isSuccessful && data.data?.username != null) {
                    UserInfoManager.saveUsername(data.data.username)
                    _verifyTokenData.value = data.data
                    Log.d("VERIFY TOKEN DATA", _verifyTokenData.value.toString())
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    UserInfoManager.saveLogin(false)
                    TokenManager.clearToken()
                    _homeUiState.value = HomeUiState.Error(errorData.msg)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

sealed class HomeUiState {

    object Loading : HomeUiState()
    object Finish : HomeUiState()
    class Error(val msg: String? = null, val isNoNetWork: Boolean = false) : HomeUiState()

}

sealed class MeUiState() {
    object NoAction : MeUiState()
    object SubmittingUsername : MeUiState()
    object SubmitFinish : MeUiState()

    class LoadError(val msg: String? = null, val isNoNetWork: Boolean = false) : MeUiState()
}