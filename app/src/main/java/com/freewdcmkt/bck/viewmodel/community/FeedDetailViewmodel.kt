package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.network.NetworkClient
import com.freewdcmkt.bck.util.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class FeedDetailViewmodel : ViewModel() {
    private var currentId: Int = 0
    private val _feedDetailUiState = MutableStateFlow<FeedDetailUiState>(FeedDetailUiState.Loading)
    val feedDetailUiState: StateFlow<FeedDetailUiState> = _feedDetailUiState.asStateFlow()
    private val _isAuthor = MutableStateFlow(false)
    val isAuthor: StateFlow<Boolean> = _isAuthor.asStateFlow()
    fun fetchData(id: Int, refresh: Boolean = false) {
        if (currentId == id && !refresh && _feedDetailUiState.value is FeedDetailUiState.Success) return
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            currentId = id
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.feedDetail(id)).build()
                    ).execute()
                }
                val body = response.body.string()
                Log.d("FEED DETAIL VIEWMODEL", body)
                Log.d("FEED VIEW MODEL", response.code.toString())
                val date = JsonParser.json.decodeFromString<BaseData<FeedDetailData>>(body)

                if (response.isSuccessful && date.data != null) {
                    val currentAccount = UserInfoManager.getUserAccountFlow().first()
                    _isAuthor.value = (currentAccount == date.data.qq)
                    _feedDetailUiState.value = FeedDetailUiState.Success(date.data)
                } else {
                    val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                    _feedDetailUiState.value = FeedDetailUiState.Error(errorData.msg)
                }
            } catch (e: Exception) {
                Log.e("FEED DETAIL VIEWMODEL", e.message.toString())
                _feedDetailUiState.value = FeedDetailUiState.Error(e.message.toString())
            }
        }
    }

    fun seedLike(id: Int, isLiked: Boolean) {
        viewModelScope.launch {
            val currentState = _feedDetailUiState.value
            if (currentState !is FeedDetailUiState.Success) return@launch

            val oldData = currentState.feedDetailData

            val newLikeCount = if (isLiked) oldData.likeCount - 1 else oldData.likeCount + 1
            val newIsLiked = !isLiked

            val updatedData = oldData.copy(
                likeCount = newLikeCount,
                isLiked = newIsLiked
            )
            _feedDetailUiState.value = FeedDetailUiState.Success(updatedData)
            val requestBody =
                buildJsonObject {
                    put("id", id)
                }.toString().toRequestBody("application/json".toMediaType())
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.LIKE_FEED_URL)
                            .post(requestBody).build()
                    ).execute()
                }
                val body = response.body.string()

                if (!response.isSuccessful) {
                    _feedDetailUiState.value = FeedDetailUiState.Success(oldData)

                }
            } catch (e: Exception) {
                _feedDetailUiState.value = FeedDetailUiState.Success(oldData)
                Log.e("FEED DETAIL VIEWMODEL", "Exception: ${e.message}", e)
            }
        }
    }

    fun replyFeed(id: Int, content: String, reply: String? = null) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        val requestBody = buildJsonObject {
            put("id", id)
            put("content", content)
            if (reply != null) put("reply", reply)
        }.toString().toRequestBody("application/json".toMediaType())
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                NetworkClient.client.newCall(
                    Request.Builder().url(RequestApi.Community.REPLY_FEED_URL).post(requestBody)
                        .build()
                ).execute()
            }
            val body = response.body.string()
            if (response.isSuccessful) {
                fetchData(id,true)
            } else {
                val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                _feedDetailUiState.value = FeedDetailUiState.Error(errorData.msg)
            }
        }
    }

    fun deleteFeed(id: Int) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.deleteFeed(id)).delete().build()
                    ).execute()
                }
                val body = response.body.string()
                if (response.isSuccessful) {
                    _feedDetailUiState.value = FeedDetailUiState.DeleteSuccess
                } else {
                    val msg = JsonParser.json.decodeFromString<ErrorData>(body)
                    _feedDetailUiState.value = FeedDetailUiState.Error(msg.msg)
                }
            } catch (e: Exception) {
                _feedDetailUiState.value = FeedDetailUiState.Error(e.message.toString())
            }
        }

    }
}

sealed class FeedDetailUiState {
    object Loading : FeedDetailUiState()
    object DeleteSuccess : FeedDetailUiState()
    class Success(val feedDetailData: FeedDetailData) : FeedDetailUiState()
    class Error(val msg: String) : FeedDetailUiState()
}