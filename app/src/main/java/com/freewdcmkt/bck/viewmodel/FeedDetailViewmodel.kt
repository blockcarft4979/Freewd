package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class FeedDetailViewmodel : ViewModel() {
    private val _feedDetailUiState = MutableStateFlow<FeedDetailUiState>(FeedDetailUiState.Loading)
    val feedDetailUiState: StateFlow<FeedDetailUiState> = _feedDetailUiState.asStateFlow()
    fun fetchData(id: Int, zone: Int) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.feedDetail(id, zone)).build()
                    ).execute()
                }
                val body = response.body.string()
                if (response.isSuccessful) {
                    val date = JsonParser.json.decodeFromString<BaseData<FeedDetailData>>(body)
                    if (date.data != null) {
                        _feedDetailUiState.value = FeedDetailUiState.Success(date.data)
                    }
                }
            } catch (e: Exception) {
                Log.d("FEED DETAIL VIEWMODEL", e.message.toString())
                _feedDetailUiState.value = FeedDetailUiState.Error(e.message.toString())
            }
        }
    }

    fun seedLike(id: Int, zone: Int, isLiked: Boolean) {
        viewModelScope.launch {
            val currentState = _feedDetailUiState.value
            if (currentState !is FeedDetailUiState.Success) return@launch

            val oldData = currentState.feedDetailData
            // 计算新值
            val newLikeCount = if (isLiked) oldData.likeCount - 1 else oldData.likeCount + 1
            val newIsLiked = !isLiked

            // 乐观更新 UI
            val updatedData = oldData.copy(
                likeCount = newLikeCount,
                isLiked = newIsLiked
            )
            _feedDetailUiState.value = FeedDetailUiState.Success(updatedData)

            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.client.newCall(
                        Request.Builder().url(RequestApi.Community.likeFeed(id, zone)).build()
                    ).execute()
                }
                Log.d("FEED DETAIL VIEWMODEL", RequestApi.Community.likeFeed(id, zone))
                Log.d("FeedDetailviewmodel", "code: ${response.code}, message: ${response.message}")
                val body = response.body?.string() ?: "null body"
                Log.d("FeedDetailviewmodel", "body: $body")

                if (!response.isSuccessful) {
                    // 请求失败，回滚数据
                    _feedDetailUiState.value = FeedDetailUiState.Success(oldData)
                    // 这里最好用 Toast 或 Snackbar 提示用户，不要直接设置 Error 状态
                    // _feedDetailUiState.value = FeedDetailUiState.Error("点赞失败")  // 不建议这样，会覆盖内容
                }
            } catch (e: Exception) {
                // 异常回滚
                _feedDetailUiState.value = FeedDetailUiState.Success(oldData)
                Log.e("FEED DETAIL VIEWMODEL", "Exception: ${e.message}", e)
                // 提示用户
            }
        }
    }
}

sealed class FeedDetailUiState {
    object Loading : FeedDetailUiState()
    class Success(val feedDetailData: FeedDetailData) : FeedDetailUiState()
    class Error(val msg: String) : FeedDetailUiState()
    //object Liked: FeedDetailUiState()
}