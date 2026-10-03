package com.freewdcmkt.bck.viewmodel.community


//import com.freewdcmkt.bck.data.ErrorData
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.data.screen.LikeFeedRequestData
import com.freewdcmkt.bck.data.screen.ReplyFeedData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.CommunityClient
import com.freewdcmkt.bck.util.network.RetroClient
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val errorMsg: StateFlow<String> = _errorMsg.asStateFlow()
    private val _isNoNetwork = MutableStateFlow(false)
    val isNoNetwork: StateFlow<Boolean> = _isNoNetwork.asStateFlow()

    fun fetchData(id: Int, refresh: Boolean = false) {
        Log.d("POST DETAIL ID", id.toString())
        if (currentId == id && !refresh && _feedDetailUiState.value is FeedDetailUiState.Success) return
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            currentId = id
            val result = safeApiCall { RetroV2Client.apiService.getPostDetails(id = id) }

            when (result) {
                is ApiResult.Success -> {
                    val data = result.data
                    if (data != null) {
                        val currentAccount = UserInfoData.account.value
                        _isAuthor.value = (currentAccount == data.qq)
                        _feedDetailData.value = data
                        _feedDetailUiState.value = FeedDetailUiState.Success
                    } else {
                        getFeedErrorHint()
                    }
                }

                is ApiResult.Error -> {
                    Log.d("FEED DETAIL", "Error: ${result.message}")
                    getFeedErrorHint()
                }

                is ApiResult.NetworkError -> {
                    _isNoNetwork.value = true
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }
            }
        }
    }

    fun getFeedErrorHint() {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            try {
                val response = CommunityClient.apiService.getFeedErrorHint()
                val data = response.body()
                Log.d("ERROR HINT", data.toString())
                if (response.isSuccessful && data?.data != null) {
                    val data = data.data
                    _feedDetailData.value = data
                    _feedDetailUiState.value = FeedDetailUiState.OnFeedErrorHint
                } else {
                    _isNoNetwork.value = true
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _isNoNetwork.value = true
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

            // 1. 乐观更新：立即改本地
            _feedDetailData.value = oldData.copy(
                likeCount = newLikeCount.coerceAtLeast(0),
                isLiked = newIsLiked
            )

            // 2. 发请求
            when (val result = safeApiCall { RetroV2Client.apiService.likePost(id) }) {
                is ApiResult.Success -> {
                    // 用服务端返回的数据覆盖，保证一致
                    result.data?.let { likeData ->
                        _feedDetailData.value = oldData.copy(
                            isLiked = likeData.isLiked,
                            likeCount = likeData.likeCount
                        )
                    }
                }

                is ApiResult.Error -> {
                    // 回滚
                    _feedDetailData.value = oldData
                    _errorMsg.value = result.message
                }

                is ApiResult.NetworkError -> {
                    // 回滚
                    _feedDetailData.value = oldData
                    _isNoNetwork.value = true
                }
            }
        }
    }

    fun replyFeed(id: Int, content: String, reply: String? = null) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            val body = ReplyFeedData(content, reply)
            Log.d("reply feed function", body.toString())
            when (val result = safeApiCall { RetroV2Client.apiService.replyPost(id, body) }) {
                is ApiResult.Success -> fetchData(id, true)
                is ApiResult.Error -> {
                    _errorMsg.value = result.message
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }

                is ApiResult.NetworkError -> {
                    _isNoNetwork.value = true
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }
            }
        }
    }

    fun deleteFeed(id: Int) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.deletePost(id) }) {
                is ApiResult.Success -> {
                    _feedDetailUiState.value = FeedDetailUiState.DeleteSuccess
                }

                is ApiResult.Error -> {
                    _errorMsg.value = result.message
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }

                is ApiResult.NetworkError -> {
                    _isNoNetwork.value = true
                    _feedDetailUiState.value = FeedDetailUiState.Error
                }
            }
        }
    }

    fun deleteReply(id: Int, rid: Int) {

        val oldData = _feedDetailData.value
        val oldReplies = oldData.reply ?: emptyList()

        val newReplies = oldReplies.filter { it.commentId != rid }
        _feedDetailData.value = oldData.copy(reply = newReplies)

        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.deleteReply(id, rid) }) {
                is ApiResult.Success -> {
                }

                is ApiResult.Error -> {
                    _feedDetailData.value = oldData
                    _errorMsg.value = result.message
                }

                is ApiResult.NetworkError -> {
                    _feedDetailData.value = oldData
                    _isNoNetwork.value = true
                }
            }
        }
    }

    fun resetUi() {
        _feedDetailUiState.value = FeedDetailUiState.Loading
    }
}

sealed class FeedDetailUiState {
    object Loading : FeedDetailUiState()
    object DeleteSuccess : FeedDetailUiState()
    object OnFeedErrorHint : FeedDetailUiState()
    object Success : FeedDetailUiState()
    object Error : FeedDetailUiState()
}
