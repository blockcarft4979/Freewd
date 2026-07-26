package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.PostFeedData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class PostFeedViewmodel : ViewModel() {
    private val _postFeedUiState = MutableStateFlow<PostFeedUiState>(PostFeedUiState.NoAction)
    val postFeedUiState: StateFlow<PostFeedUiState> = _postFeedUiState.asStateFlow()
    fun postFeed(zone: Int, message: String? = null, title: String? = null) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    _postFeedUiState.value = PostFeedUiState.Upload
                    val feedData: JsonObject = buildJsonObject {
                        put("zone", zone)
                        put("message", message)
                        put("title", title)
                    }
                    val body = feedData.toString().toRequestBody("application/json".toMediaType())
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.POST_FEED_URL).post(body).build()
                    ).execute()
                }
                val body = response.body.string()
                if (response.isSuccessful) {
                    _postFeedUiState.value = PostFeedUiState.Success
                }else{
                    Log.d("POST FEED VIEWMODEL",body+response.code)
                    _postFeedUiState.value = PostFeedUiState.Error(JsonParser.json.decodeFromString<BaseData<PostFeedData>>(body).msg)
                }
            } catch (e: Exception) {
                _postFeedUiState.value = PostFeedUiState.Error(e.message.toString())
            }
        }
    }
}

sealed class PostFeedUiState {
    object NoAction : PostFeedUiState()
    object Upload : PostFeedUiState()
    object Success : PostFeedUiState()
    class Error(val msg: String?) : PostFeedUiState()
}