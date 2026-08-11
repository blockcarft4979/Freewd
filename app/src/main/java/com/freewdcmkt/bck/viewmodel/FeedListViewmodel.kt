package com.freewdcmkt.bck.viewmodel

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.api.RequestApi
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.ErrorData
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

//你妈的傻逼 FEED LIST VIEWMODEL
// 看我今天下午不把你给杀了
// TODO()
class FeedListViewmodel : ViewModel() {
    private var currentPage = 0
    private var totalPages = 0
    private var hasMore = true
    private var currentZone: Int? = null
    private var isLoadingMore = false

    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()

    val listState = LazyListState()

    // 首次加载或下拉刷新
    fun fetchData(zone: Int, forceRefresh: Boolean = false) {
        Log.d("FeedVM", "fetchData called, forceRefresh=$forceRefresh")
        if (!forceRefresh && currentZone == zone && _feedUiState.value is FeedUiState.Success) {
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
                        val oldList = (_feedUiState.value as? FeedUiState.Success)?.feedData?.feed ?: emptyList()
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
                    hasMore = false
                    val current = _feedUiState.value
                    if (current is FeedUiState.Success) {
                        _feedUiState.value = current.copy(
                            isLoadingMore = false,
                            hasMore = false,
                            error = "数据为空"
                        )
                    } else {
                        _feedUiState.value = FeedUiState.Error("数据为空")
                    }
                }
            } else {
                // HTTP 非 2xx 响应
                val errorData = JsonParser.json.decodeFromString<ErrorData>(body)
                val errorMsg = errorData.msg ?: "请求失败"
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