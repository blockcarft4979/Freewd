package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.ErrorData
import com.freewdcmkt.bck.data.screen.FeedData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.network.RetroClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

//你妈的傻逼 FEED LIST VIEWMODEL
// 看我今天下午不把你给杀了
// TODO()
class FeedListViewmodel() : ViewModel() {
    private var currentPage = 0
    private var totalPages = 0
    private var hasMore = true
    private var currentZone: Int? = null
    private var isLoadingMore = false

    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()

    val listState = LazyListState()

    init {
        //fetchData(zone, true)
    }

    // 首次加载或下拉刷新
    fun fetchData(zone: Int, forceRefresh: Boolean = false) {

        Log.d(
            "FeedVM",
            "fetchData called, forceRefresh=$forceRefresh $currentZone ${_feedUiState.value} $zone"
        )
        if (!forceRefresh && currentZone == zone && _feedUiState.value is FeedUiState.Success) return

        currentZone = zone
        currentPage = 0
        totalPages = 0
        hasMore = true
        isLoadingMore = false
        _feedUiState.value = FeedUiState.Loading
        viewModelScope.launch {
            loadPage(zone, page = 1, isAppend = false)
        }
        Log.d(
            "FeedVM",
            "fetchData called, forceRefresh=$forceRefresh $currentZone ${_feedUiState.value} $zone"
        )

    }

    // 加载更多（追加）
    fun loadMore() {
        val zone = currentZone ?: return
        // 条件检查：没有更多、正在加载更多、当前处于加载状态，都直接返回
        if (!hasMore) {
            Log.d("FEED VM", "loadMore: hasMore=false, 不再加载")
            return
        }
        if (isLoadingMore) {
            Log.d("FEED VM", "loadMore: 已在加载中")
            return
        }
        if (_feedUiState.value is FeedUiState.Loading) {
            Log.d("FEED VM", "loadMore: 当前是 Loading 状态，等待")
            return
        }

        isLoadingMore = true
        // 更新 UI 状态，显示底部加载指示器（保留原列表）
        val currentData =
            (_feedUiState.value as? FeedUiState.Success)?.feedData ?: FeedData(0, 0, emptyList())
        _feedUiState.value = FeedUiState.Success(
            feedData = currentData,
            isLoadingMore = true,
            hasMore = hasMore
        )

        viewModelScope.launch {
            loadPage(zone, page = currentPage + 1, isAppend = true)
        }
    }

    private suspend fun loadPage(zone: Int, page: Int, isAppend: Boolean) {
        try {
            val response = RetroClient.apiService.getFeed(zone,page)

            if (response.data != null) {

                val feedData = response.data
                totalPages = feedData.pages
                currentPage = feedData.page
                hasMore = currentPage < totalPages

                val newFeed = if (isAppend) {
                    val oldList = (_feedUiState.value as? FeedUiState.Success)?.feedData?.feed
                        ?: emptyList()
                    val merged = (oldList + feedData.feed).distinctBy { it.id }
                    feedData.copy(feed = merged)
                } else {
                    feedData
                }

                _feedUiState.value = FeedUiState.Success(
                    feedData = newFeed,
                    isLoadingMore = false,
                    hasMore = hasMore,
                    error = null
                )
                Log.d("FEED VM", "加载成功，当前条目数: ${newFeed.feed.size}, hasMore=$hasMore")
            } else {
                val errorMsg = response.msg
                val current = _feedUiState.value
                if (current is FeedUiState.Success) {
                    _feedUiState.value = current.copy(
                        isLoadingMore = false,
                        error = errorMsg
                    )
                } else {
                    _feedUiState.value = FeedUiState.Error(errorMsg)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val current = _feedUiState.value
            if (current is FeedUiState.Success) {
                _feedUiState.value = current.copy(
                    isLoadingMore = false,
                    error = ""
                )
            } else {
                _feedUiState.value = FeedUiState.Error(isNoNetwork = true)
            }
        } finally {
            isLoadingMore = false
        }
    }
}

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(
        val feedData: FeedData,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true,
        val error: String? = null
    ) : FeedUiState()

    class Error(val msg: String? = null, val isNoNetwork: Boolean = false) : FeedUiState()
}