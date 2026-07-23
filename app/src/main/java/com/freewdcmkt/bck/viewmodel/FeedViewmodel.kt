package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.FeedData
import com.freewdcmkt.bck.util.JsonParser
import com.freewdcmkt.bck.util.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request

class FeedViewmodel : ViewModel() {
    private var currentPage = 0          // 当前已加载的页码（0 表示未加载）
    private var totalPages = 0           // 总页数
    private var hasMore = true           // 是否还有更多
    private var currentZone: Int? = null
    private var isLoadingMore = false    // 防止重复加载

    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()

    val listState = LazyListState()

    // 首次加载或下拉刷新
    fun fetchData(zone: Int) {
        if (currentZone == zone && _feedUiState.value is FeedUiState.Success) {
            Log.d("FEED VM", "fetchData: 数据已存在，不再重复请求")
            return
        }
        currentZone = zone
        currentPage = 0
        totalPages = 0
        hasMore = true
        isLoadingMore = false
        _feedUiState.value = FeedUiState.Loading
        viewModelScope.launch {
            loadPage(zone, page = 1, isAppend = false)
        }
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
        val currentData = (_feedUiState.value as? FeedUiState.Success)?.feedData ?: FeedData(0, 0,emptyList())
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
            val response = withContext(Dispatchers.IO) {
                NetworkClient.client.newCall(
                    Request.Builder().url(RequestApi.Community.feed(zone, page)).build()
                ).execute()
            }
            val body = response.body.string()
            Log.d("FEED VM", "loadPage response: $body")

            if (response.isSuccessful) {
                val data = JsonParser.json.decodeFromString<BaseData<FeedData>>(body)
                if (data.data != null) {
                    val feedData = data.data
                    totalPages = feedData.pages
                    currentPage = feedData.page
                    hasMore = currentPage < totalPages

                    val newFeed = if (isAppend) {
                        // 合并旧数据 + 新数据，并去重（按 id）
                        val oldList = (_feedUiState.value as? FeedUiState.Success)?.feedData?.feed ?: emptyList()
                        val merged = (oldList + feedData.feed).distinctBy { it.id }
                        feedData.copy(feed = merged)
                    } else {
                        feedData
                    }

                    _feedUiState.value = FeedUiState.Success(
                        feedData = newFeed,
                        isLoadingMore = false,
                        hasMore = hasMore
                    )
                    Log.d("FEED VM", "加载成功，当前条目数: ${newFeed.feed.size}, hasMore=$hasMore")
                } else {
                    // 服务器返回 data 为空，视为没有更多
                    hasMore = false
                    _feedUiState.value = (_feedUiState.value as? FeedUiState.Success)?.copy(
                        isLoadingMore = false,
                        hasMore = false
                    ) ?: FeedUiState.Error("数据为空")
                }
            } else {
                val errorData = JsonParser.json.decodeFromString<BaseData<Nothing>>(body)
                _feedUiState.value = FeedUiState.Error(errorData.msg ?: "加载失败")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _feedUiState.value = FeedUiState.Error(e.message.toString())
        } finally {
            isLoadingMore = false
        }
    }
}

// 修改 FeedUiState，携带加载更多状态
sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(
        val feedData: FeedData,
        val isLoadingMore: Boolean = false,
        val hasMore: Boolean = true
    ) : FeedUiState()
    class Error(val msg: String) : FeedUiState()
}