package com.freewdcmkt.bck.viewmodel.community


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.request.LikeFeedRequestData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.network.NetworkClient
import com.freewdcmkt.bck.util.network.RetroClient
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

class FeedDetailViewmodel : ViewModel() {
    private var currentId: Int = 0
    private val _feedDetailData = MutableStateFlow(
        FeedDetailData(
            qq = "",
            username = "",
            date = "",
            likeCount = 0,
            isLiked = false,
            isMarkdown = false
        )
    )
    val feedDetailData: StateFlow<FeedDetailData> = _feedDetailData.asStateFlow()
    private val _feedDetailUiState = MutableStateFlow<FeedDetailUiState>(FeedDetailUiState.Loading)
    val feedDetailUiState: StateFlow<FeedDetailUiState> = _feedDetailUiState.asStateFlow()
    private val _isAuthor = MutableStateFlow(false)
    val isAuthor: StateFlow<Boolean> = _isAuthor.asStateFlow()
    private val _errorMsg = MutableStateFlow("")
    val errorMsg : StateFlow<String> = _errorMsg.asStateFlow()
    fun fetchData(id: Int, refresh: Boolean = false) {
        if (currentId == id && !refresh && _feedDetailUiState.value is FeedDetailUiState.Success) return
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            currentId = id
            try {
                val response = RetroClient.apiService.getFeedDetail(id)
                val data = response.body()
                Log.d("FEED DETAIL VIEWMODEL", "$response ${response.code()}")
                if (response.isSuccessful && data?.data != null) {
                    val currentAccount = UserInfoData.account.value
                    val data = data.data
                    _isAuthor.value = (currentAccount == data.qq)
                    _feedDetailData.value = data
                    _feedDetailUiState.value = FeedDetailUiState.Success
                } else {
                    val errorData = response.errorBody()?.string() ?: ""
                    val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                    _errorMsg.value = errorMsg.msg?:""
                    _feedDetailUiState.value = FeedDetailUiState.Error
                    Log.d("FEED DETAIL VIEWMODEL", errorData)
                }

            } catch (e: Exception) {
                _feedDetailUiState.value = FeedDetailUiState.Error
            }
        }
    }


    fun seedLike(id: Int, isLiked: Boolean) {
        viewModelScope.launch {
            val currentState = _feedDetailUiState.value
            if (currentState !is FeedDetailUiState.Success) return@launch

            val oldData = feedDetailData.value

            val newLikeCount = if (isLiked) oldData.likeCount - 1 else oldData.likeCount + 1
            val newIsLiked = !isLiked

            val updatedData = oldData.copy(
                likeCount = newLikeCount,
                isLiked = newIsLiked
            )
            _feedDetailData.value = updatedData
            _feedDetailUiState.value = FeedDetailUiState.Success

            try {
                val response = RetroClient.apiService.replyFeed(LikeFeedRequestData((id)))
                val data = response.body()
                if (response.isSuccessful&&data?.data == null) {
                    _feedDetailData.value = oldData
                    _feedDetailUiState.value = FeedDetailUiState.Success
                } else {
                    val errorData = response.errorBody()?.string() ?: ""
                    val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                    _errorMsg.value = errorMsg.msg?:""
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }
            } catch (e: Exception) {
                _feedDetailData.value = oldData
                _feedDetailUiState.value = FeedDetailUiState.Success
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
                fetchData(id, true)
            } else {
                val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                _errorMsg.value = errorData.msg
                _feedDetailUiState.value = FeedDetailUiState.Error
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
                    _errorMsg.value = msg.msg
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }
            } catch (e: Exception) {
                _errorMsg.value = e.message.toString()
                _feedDetailUiState.value = FeedDetailUiState.Error
            }
        }

    }
}

sealed class FeedDetailUiState {
    object Loading : FeedDetailUiState()
    object DeleteSuccess : FeedDetailUiState()
    object Success : FeedDetailUiState()
    object Error : FeedDetailUiState()
}
