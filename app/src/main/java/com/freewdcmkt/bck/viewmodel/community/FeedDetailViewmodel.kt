package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.data.screen.FeedReplyData
import com.freewdcmkt.bck.data.screen.ReplyFeedData
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.CommunityClient
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import com.freewdcmkt.bck.util.time.formatTime
import com.freewdcmkt.bck.util.time.getSystemDate
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // 一次性事件：操作级提示走 snackbar，不再掀翻整页
    private val _events = MutableSharedFlow<FeedDetailEvent>(extraBufferCapacity = 8)
    val events: SharedFlow<FeedDetailEvent> = _events.asSharedFlow()

    // 发送中：VM 层防抖 + UI 可用于禁用发送按钮
    private val _isSendingReply = MutableStateFlow(false)
    val isSendingReply: StateFlow<Boolean> = _isSendingReply.asStateFlow()

    // 负数临时 commentId，与真实 id 不会冲突
    private var tempIdSeq = -1

    fun fetchData(id: Int, refresh: Boolean = false, silent: Boolean = false) {
        Log.d("POST DETAIL ID", id.toString())
        if (currentId == id && !refresh && _feedDetailUiState.value is FeedDetailUiState.Success) return
        if (!silent) _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            currentId = id
            when (val result = safeApiCall { RetroV2Client.apiService.getPostDetails(id = id) }) {
                is ApiResult.Success -> {
                    val data = result.data
                    if (data != null) {
                        _isAuthor.value = data.canDelete
                        _feedDetailData.value = data
                        _feedDetailUiState.value = FeedDetailUiState.Success
                    } else {
                        getFeedErrorHint()
                    }
                }

                is ApiResult.Error -> {
                    Log.d("FEED DETAIL", "Error: ${result.message}")
                    if (silent) return@launch
                    getFeedErrorHint()
                }

                is ApiResult.NetworkError -> {
                    _isNoNetwork.value = true
                    if (silent) return@launch
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
                    _feedDetailData.value = data.data
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
            if (_feedDetailUiState.value !is FeedDetailUiState.Success) return@launch

            val oldData = _feedDetailData.value
            val newLikeCount = if (isLiked) oldData.likeCount - 1 else oldData.likeCount + 1
            val newIsLiked = !isLiked

            _feedDetailData.value = oldData.copy(
                likeCount = newLikeCount.coerceAtLeast(0),
                isLiked = newIsLiked
            )

            when (val result = safeApiCall { RetroV2Client.apiService.likePost(id) }) {
                is ApiResult.Success -> {
                    result.data?.let { likeData ->
                        // 用当前值 copy，避免覆盖期间其他变更（评论/删评）
                        _feedDetailData.value = _feedDetailData.value.copy(
                            isLiked = likeData.isLiked,
                            likeCount = likeData.likeCount
                        )
                    }
                }

                is ApiResult.Error -> {
                    _feedDetailData.value = oldData
                    emitError(R.string.like_failed_hint, result.message)
                }

                is ApiResult.NetworkError -> {
                    _feedDetailData.value = oldData
                    emitError(R.string.no_internet_hint)
                }
            }
        }
    }

    /**
     * 发评论：乐观插入 → 成功静默刷新 → 失败回滚 + snackbar
     * 注意：FeedReplyData 没有 replyQq/replyUsername/isPending 字段，
     *      楼内回复的展示交给后端返回，临时项按普通评论显示。
     */
    fun replyFeed(id: Int, content: String, replyQq: String? = null) {
        if (content.isBlank()) return
        if (_isSendingReply.value) return

        _isSendingReply.value = true

        val tempId = tempIdSeq--
        val tempReply = FeedReplyData(
            uid = UserInfoData.uid.value,
            commentId = tempId,
            date = formatTime(getSystemDate("yyyy-MM-dd HH:mm:ss")),
            msg = content,
            qq = UserInfoData.account.value,
            canDelete = true,
            username = UserInfoData.username.value,
        )

        val oldData = _feedDetailData.value
        val oldReplies = oldData.reply ?: emptyList()
        _feedDetailData.value = oldData.copy(reply = oldReplies + tempReply)

        viewModelScope.launch {
            val body = ReplyFeedData(content, replyQq)
            when (safeApiCall { RetroV2Client.apiService.replyPost(id, body) }) {
                is ApiResult.Success -> {
                    // 后端返回类型不确定 → 直接静默刷一次，保证数据一致且不闪 Loading
                    silentRefresh(id)
                    _isSendingReply.value = false
                }

                is ApiResult.Error -> {
                    rollbackTempReply(tempId)
                    _isSendingReply.value = false
                    emitError(R.string.reply_failed_hint, null)
                }

                is ApiResult.NetworkError -> {
                    rollbackTempReply(tempId)
                    _isSendingReply.value = false
                    emitError(R.string.no_internet_hint)
                }
            }
        }
    }

    private fun rollbackTempReply(tempId: Int) {
        val current = _feedDetailData.value
        val filtered = (current.reply ?: emptyList()).filterNot { it.commentId == tempId }
        _feedDetailData.value = current.copy(reply = filtered)
    }

    private suspend fun silentRefresh(id: Int) {
        val result = safeApiCall { RetroV2Client.apiService.getPostDetails(id) }
        if (result is ApiResult.Success) {
            result.data?.let {
                _feedDetailData.value = it
                _isAuthor.value = it.canDelete
            }
        }
        // 静默失败就保住乐观数据，下次进页面自然会拉到
    }

    fun deleteFeed(id: Int) {
        _feedDetailUiState.value = FeedDetailUiState.Loading
        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.deletePost(id) }) {
                is ApiResult.Success -> {
                    _feedDetailUiState.value = FeedDetailUiState.DeleteSuccess
                }

                is ApiResult.Error -> {
                    _feedDetailUiState.value = FeedDetailUiState.Success
                    emitError(R.string.delete_feed_failed_hint, result.message)
                }

                is ApiResult.NetworkError -> {
                    _feedDetailUiState.value = FeedDetailUiState.Success
                    emitError(R.string.no_internet_hint)
                }
            }
        }
    }

    fun deleteReply(id: Int, rid: Int) {
        val oldData = _feedDetailData.value
        val oldReplies = oldData.reply ?: emptyList()
        val removedIndex = oldReplies.indexOfFirst { it.commentId == rid }
        if (removedIndex == -1) return
        val removed = oldReplies[removedIndex]

        // 乐观删除
        _feedDetailData.value = oldData.copy(
            reply = oldReplies.filter { it.commentId != rid }
        )

        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.deleteReply(id, rid) }) {
                is ApiResult.Success -> Unit

                is ApiResult.Error -> {
                    rollbackReply(removedIndex, removed)
                    emitError(R.string.delete_reply_failed_hint, result.message)
                }

                is ApiResult.NetworkError -> {
                    rollbackReply(removedIndex, removed)
                    emitError(R.string.no_internet_hint)
                }
            }
        }
    }

    private fun rollbackReply(index: Int, removed: FeedReplyData) {
        val current = _feedDetailData.value
        val list = (current.reply ?: emptyList()).toMutableList()
        list.add(index.coerceIn(0, list.size), removed)
        _feedDetailData.value = current.copy(reply = list)
    }

    private fun emitError(@StringRes messageRes: Int, serverMessage: String? = null) {
        _events.tryEmit(FeedDetailEvent.ShowError(messageRes, serverMessage))
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

sealed class FeedDetailEvent {
    /**
     * serverMessage 非空时优先展示（后端返回的运行时文案），
     * 否则用 messageRes 兜底。
     */
    data class ShowError(
        @StringRes val messageRes: Int,
        val serverMessage: String? = null,
    ) : FeedDetailEvent()

    data class ShowMessage(@StringRes val messageRes: Int) : FeedDetailEvent()
}