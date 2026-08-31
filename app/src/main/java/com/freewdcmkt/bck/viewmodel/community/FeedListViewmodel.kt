package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.BaseData
import com.freewdcmkt.bck.data.screen.Feed
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
    private var _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()
    private var currentZone: Int? = null
    private var _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()
    private val _isNoNetwork = MutableStateFlow(false)
    val isNoNetwork: StateFlow<Boolean> = _isNoNetwork.asStateFlow()
    private val _feedUiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val feedUiState: StateFlow<FeedUiState> = _feedUiState.asStateFlow()
    private val _feedListData = MutableStateFlow<FeedData?>(null)
    val feedListData: StateFlow<FeedData?> = _feedListData.asStateFlow()
    private val _errorMsg = MutableStateFlow("")
    val errorMsg: StateFlow<String> = _errorMsg.asStateFlow()
    val listState = LazyListState()

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
        _hasMore.value = true
        _isLoadingMore.value = false
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

        if (!hasMore.value || _isLoadingMore.value) {
            return
        }

        _isLoadingMore.value = true

        _feedUiState.value = FeedUiState.Success
        _isLoadingMore.value = true
        _hasMore.value = hasMore.value

        viewModelScope.launch {
            loadPage(zone, page = currentPage + 1, isAppend = true)
        }
    }

    private suspend fun loadPage(zone: Int, page: Int, isAppend: Boolean) {
        try {
            val response = RetroClient.apiService.getFeed(zone, page)
            val data = response.body()
            if (data?.data != null) {
                val feedData = data.data
                totalPages = feedData.pages
                currentPage = feedData.page
                _hasMore.value = currentPage < totalPages
                val newFeed = if (isAppend) {
                    val oldList: List<Feed> = feedListData.value?.feed ?: emptyList()
                    val newList: List<Feed> = feedData.feed
                    val merged = (oldList + newList).distinctBy { it.id }
                    feedData.copy(feed = merged)
                } else {
                    feedData
                }
                _feedListData.value = newFeed
                _feedUiState.value = FeedUiState.Success
                _isLoadingMore.value = false
                _hasMore.value = hasMore.value
            } else {
                val errorData = response.errorBody()?.string()?:""
                val errorMsg = JsonParser.json.decodeFromString<BaseData<Nothing>>(errorData)
                val current = _feedUiState.value
                if (current is FeedUiState.Success) {
                    _isLoadingMore.value = false
                } else {
                    _errorMsg.value = errorMsg.msg.toString()
                    _isNoNetwork.value = true
                    _feedUiState.value = FeedUiState.Error
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            val current = _feedUiState.value
            if (current is FeedUiState.Success) {
                _isLoadingMore.value = false
            } else {
                _isNoNetwork.value = true
                _feedUiState.value = FeedUiState.Error
            }
        } finally {
            _isLoadingMore.value = false
        }
    }
}

sealed class FeedUiState {
    object Loading : FeedUiState()
    object Success : FeedUiState()

    object Error : FeedUiState()
}