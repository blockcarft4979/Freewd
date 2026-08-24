package com.freewdcmkt.bck.viewmodel.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.UsernameData
import com.freewdcmkt.bck.layout.ui.user.MeUiState
import com.freewdcmkt.bck.util.JsonParser.json
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.util.network.NetworkClient
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

class MeViewModel: ViewModel() {
    private val _uiState = MutableStateFlow<MeUiState>(MeUiState.NoAction)
    val uiState: StateFlow<MeUiState> = _uiState.asStateFlow()
    private val _isShowNoNetwork = MutableStateFlow(false)
    val isShowNoNetwork: StateFlow<Boolean> = _isShowNoNetwork.asStateFlow()

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
                val data = json.decodeFromString<BaseData<UsernameData>>(body)

                if (response.isSuccessful && data.data != null) {

                    _uiState.value = MeUiState.SubmitFinish
                    UserInfoManager.saveUsername(data.data.username)
                } else {
                    val errorData = json.decodeFromString<ErrorData>(body)
                    _uiState.value = MeUiState.LoadError(errorData.msg)
                }
            } catch (e: Exception) {
                _isShowNoNetwork.value = true
                _uiState.value = MeUiState.LoadError(isNoNetWork = true)
            }
        }
    }
}