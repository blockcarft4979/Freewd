package com.freewdcmkt.bck.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.NotificationData
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

class NotificationViewmodel : ViewModel() {
    private var isLoaded: Boolean = false
    private val _uiStates = MutableStateFlow<NotificationUiStates>(NotificationUiStates.Loading)
    val uiStates: StateFlow<NotificationUiStates> = _uiStates.asStateFlow()

    /** GET /notifications（v2，等价于 v1 的 notification/get_notification.php） */
    fun getNotification() {
        if (isLoaded) {
            return
        }
        _uiStates.value = NotificationUiStates.Loading
        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.getNotifications() }) {
                is ApiResult.Success -> {
                    isLoaded = true
                    UserInfoData.clearUnreadCount()
                    _uiStates.value = NotificationUiStates.Finish(result.data?.list ?: emptyList())
                }

                is ApiResult.Error -> {
                    _uiStates.value = NotificationUiStates.LoadError(result.message)
                }

                is ApiResult.NetworkError -> {
                    _uiStates.value = NotificationUiStates.LoadError(isNoNetwork = true)
                }
            }
        }
    }


    /** POST /notifications/clear（v2，等价于 v1 的 notification/clear_notification.php） */
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
            when (val result = safeApiCall { RetroV2Client.apiService.clearNotifications(requestBody) }) {
                is ApiResult.Success -> {
                    _uiStates.value = NotificationUiStates.Finish(emptyList())
                }

                is ApiResult.Error -> {
                    _uiStates.value = NotificationUiStates.LoadError(result.message)
                }

                is ApiResult.NetworkError -> {
                    _uiStates.value = NotificationUiStates.LoadError(isNoNetwork = true)
                }
            }
        }

    }
}

sealed class NotificationUiStates() {
    object Loading : NotificationUiStates()
    class LoadError(val msg: String? = null, val isNoNetwork: Boolean = false) :
        NotificationUiStates()

    class Finish(val notificationData: List<NotificationData>) : NotificationUiStates()
}
