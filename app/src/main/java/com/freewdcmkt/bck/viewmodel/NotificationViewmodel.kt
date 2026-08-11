package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.NotificationData
import com.freewdcmkt.bck.data.screen.NotificationDataList
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class NotificationViewmodel : ViewModel() {
    private var isLoaded: Boolean = false
    private val _uiStates = MutableStateFlow<NotificationUiStates>(NotificationUiStates.Loading)
    val uiStates: StateFlow<NotificationUiStates> = _uiStates.asStateFlow()
    fun getNotification() {
        if (isLoaded) {
            return
        }
        _uiStates.value = NotificationUiStates.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Notification.NOTIFICATION_URL).build()
                    ).execute()
                }
                val body = response.body.string()
                Log.d("NOTIFICATION VIEWMODEL", body)
                val data = JsonParser.json.decodeFromString<BaseData<NotificationDataList>>(body)
                Log.d("NOTIFICATION VIEWMODEL DATA", data.toString())
                if (response.isSuccessful && data.data != null) {
                    isLoaded = true
                    _uiStates.value = NotificationUiStates.Finish(data.data.list)
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _uiStates.value = NotificationUiStates.LoadError(errorData.msg)
                }
            } catch (e: Exception) {
                _uiStates.value = NotificationUiStates.LoadError(isNoNetwork = true)
            }
        }
        Log.d("NOTIFICATION VIEWMODEL" ,_uiStates.value.toString())
    }

    fun clearNotifications(isAll: Boolean) {
        if (!isLoaded) {
            return
        }
        _uiStates.value = NotificationUiStates.Loading
        val requestBody =
            buildJsonObject {
                if (isAll) put("mode", "all") else {
                    put("mode", "before_days")
                    put("days", 7)
                }
            }.toString().toRequestBody("application/json".toMediaType())

            viewModelScope.launch {

                try {
                    val response = withContext(Dispatchers.IO) {
                        NetworkClient.client.newCall(
                            Request.Builder().url(RequestApi.Notification.CLEAR_ALL_NOTIFICATIONS)
                                .post(requestBody).build()
                        ).execute()
                    }
                    val body = response.body.string()
                    if (response.isSuccessful) {
                        _uiStates.value = NotificationUiStates.Finish(emptyList())
                    } else {
                        val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                        _uiStates.value = NotificationUiStates.LoadError(errorData.msg)
                    }
                } catch (e: Exception) {
                    _uiStates.value = NotificationUiStates.LoadError(isNoNetwork = true)
                }

            }

    }
}

sealed class NotificationUiStates() {
    object Loading : NotificationUiStates()
    class LoadError(val msg: String? = null, val isNoNetwork: Boolean = false) : NotificationUiStates()

    class Finish(val notificationData: List<NotificationData>) : NotificationUiStates()
}