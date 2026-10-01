package com.freewdcmkt.bck.viewmodel.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.screen.FeedData
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedListViewmodel() : ViewModel() {
    private var currentPage = 0
    private var totalPages = 0
    private var _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private var _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val _isNoNetwork = MutableStateFlow(false)
    val isNoNetwork: StateFlow<Boolean> = _isNoNetwork.asStateFlow()
    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()
    private val _feedListData = MutableStateFlow<FeedData?>(null)
    val feedListData: StateFlow<FeedData?> = _feedListData.asStateFlow()
    private val _errorMsg = MutableStateFlow("")

    init {
        fetchData(true)
    }

    fun fetchData(forceRefresh: Boolean = false) {

        if (!forceRefresh && _feedUiState.value is FeedUiState.Success) return

        currentPage = 0
        totalPages = 0
        _hasMore.value = true
        _isLoadingMore.value = false
        _feedUiState.value = FeedUiState.Loading
        viewModelScope.launch {
            loadPage(page = 1, isAppend = false)
        }

    }

    fun loadMore() {
        if (!hasMore.value || _isLoadingMore.value) return
        _isLoadingMore.value = true
        viewModelScope.launch {
            loadPage(page = currentPage + 1, isAppend = true)
        }
    }
    fun toggleLike(postId: Int) {
        val currentData = _feedListData.value ?: return
        val feed = currentData.feed
        val index = feed.indexOfFirst { it.id == postId }
        if (index == -1) return

        val target = feed[index]
        val willLike = !target.isLiked
        val newCount = if (willLike) target.likeCount + 1 else (target.likeCount - 1).coerceAtLeast(0)

        // 1. 乐观更新：立即改本地数据
        val updatedList = feed.toMutableList().apply {
            this[index] = target.copy(
                isLiked = willLike,
                likeCount = newCount
            )
        }
        _feedListData.value = currentData.copy(feed = updatedList)

        // 2. 发网络请求
        viewModelScope.launch {
            val result = safeApiCall { RetroV2Client.apiService.likePost(postId) }
            if (result !is ApiResult.Success) {
                // 3. 失败回滚
                val rollbackList = feed.toMutableList().apply {
                    this[index] = target
                }
                _feedListData.value = currentData.copy(feed = rollbackList)
                // 可选：弹一个 Snackbar 提示
            } else {
                // 可选：用服务端返回的 count 覆盖（保证一致性）
                result.data?.let { likeData ->
                    val syncedList = _feedListData.value?.feed?.toMutableList() ?: return@let
                    val idx = syncedList.indexOfFirst { it.id == postId }
                    if (idx != -1) {
                        syncedList[idx] = syncedList[idx].copy(
                            isLiked = likeData.isLiked,
                            likeCount = likeData.likeCount
                        )
                        _feedListData.value = _feedListData.value?.copy(feed = syncedList)
                    }
                }
            }
        }
    }
    private suspend fun loadPage(page: Int, isAppend: Boolean) {
        val result = safeApiCall { RetroV2Client.apiService.getPosts(page) }

        when (result) {
            is ApiResult.Success -> {
                val feedData = result.data
                if (feedData == null) {
                    handleError("数据异常")
                    return
                }
                totalPages = feedData.pages
                currentPage = feedData.page
                _hasMore.value = currentPage < totalPages

                val newFeed = if (isAppend) {
                    val oldList = feedListData.value?.feed ?: emptyList()
                    val merged = (oldList + feedData.feed).distinctBy { it.id }
                    feedData.copy(feed = merged)
                } else {
                    feedData
                }
                _feedListData.value = newFeed
                _feedUiState.value = FeedUiState.Success
            }

            is ApiResult.Error -> handleError(result.message)

            is ApiResult.NetworkError -> {
                _isNoNetwork.value = true
                val current = _feedUiState.value
                if (current !is FeedUiState.Success) {
                    _feedUiState.value = FeedUiState.Error
                }
            }
        }
        _isLoadingMore.value = false
    }

    private fun handleError(message: String) {
        _errorMsg.value = message
        val current = _feedUiState.value
        if (current !is FeedUiState.Success) {
            _feedUiState.value = FeedUiState.Error
        }
        _isLoadingMore.value = false
    }
}

sealed class FeedUiState {
    object Loading : FeedUiState()
    object Success : FeedUiState()

    object Error : FeedUiState()
}