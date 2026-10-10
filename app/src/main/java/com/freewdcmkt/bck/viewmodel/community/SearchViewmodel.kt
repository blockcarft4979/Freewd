package com.freewdcmkt.bck.viewmodel.community

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.screen.PostsData
import com.freewdcmkt.bck.data.screen.SearchMode
import com.freewdcmkt.bck.data.screen.SearchUserData
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 搜索（帖子 / 用户）
 *
 * - 帖子：v2 GET /search?q=
 * - 用户：v2 GET /users/search?q=
 *
 * 输入不触发搜索，只有用户点搜索按钮或键盘搜索键时才真正请求。
 * 切换搜索模式时，如果已有关键词会立即重搜（不想自动重搜可以去掉 onModeChange 里的 search()）。
 */
class SearchViewmodel : ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val _mode = MutableStateFlow(SearchMode.POST)
    val mode: StateFlow<SearchMode> = _mode.asStateFlow()

    // 当前关键词（输入框内容由 UI 层用 rememberSaveable 持有，这里只存一份副本）
    private var keyword = ""

    private val _posts = MutableStateFlow<List<PostsData>>(emptyList())
    val posts: StateFlow<List<PostsData>> = _posts.asStateFlow()

    private val _users = MutableStateFlow<List<SearchUserData>>(emptyList())
    val users: StateFlow<List<SearchUserData>> = _users.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    /** 本次关键词是否已经搜过（区分「还没搜」和「搜了没结果」） */
    private val _isSearched = MutableStateFlow(false)
    val isSearched: StateFlow<Boolean> = _isSearched.asStateFlow()

    private var currentPage = 0
    private var totalPages = 0
    private var searchJob: Job? = null

    private val pageSize = 20

    /**
     * 输入框变化：只更新关键词，不触发搜索。
     * 清空时立刻回到初始态（避免旧结果显示在空输入下）。
     */
    fun onKeywordChange(value: String) {
        keyword = value
        if (value.isBlank()) {
            searchJob?.cancel()
            _posts.value = emptyList()
            _users.value = emptyList()
            _uiState.value = SearchUiState.Idle
            _isSearched.value = false
            _hasMore.value = false
        }
    }

    /** 键盘搜索键 / 搜索按钮：真正发起搜索 */
    fun onSearchAction() {
        search()
    }

    /** 切换 帖子/用户：如果已有关键词，立即按新模式重搜 */
    fun onModeChange(newMode: SearchMode) {
        if (_mode.value == newMode) return
        _mode.value = newMode
        if (keyword.isNotBlank()) {
            search()
        } else {
            _uiState.value = SearchUiState.Idle
        }
    }

    fun retry() {
        search()
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value) return
        if (_uiState.value !is SearchUiState.Success) return
        _isLoadingMore.value = true
        viewModelScope.launch {
            loadPage(keyword.trim(), currentPage + 1, isAppend = true)
        }
    }

    private fun search() {
        val q = keyword.trim()
        if (q.isEmpty()) {
            _uiState.value = SearchUiState.Idle
            return
        }
        searchJob?.cancel()
        currentPage = 0
        totalPages = 0
        _hasMore.value = false
        _isSearched.value = false
        _uiState.value = SearchUiState.Loading
        searchJob = viewModelScope.launch {
            loadPage(q, 1, isAppend = false)
        }
    }

    private suspend fun loadPage(q: String, page: Int, isAppend: Boolean) {
        when (_mode.value) {
            SearchMode.POST -> loadPosts(q, page, isAppend)
            SearchMode.USER -> loadUsers(q, page, isAppend)
        }
        _isLoadingMore.value = false
    }

    private suspend fun loadPosts(q: String, page: Int, isAppend: Boolean) {
        when (val result = safeApiCall { RetroV2Client.apiService.searchPosts(q, page, pageSize) }) {
            is ApiResult.Success -> {
                val data = result.data
                if (data == null) {
                    if (!isAppend) _uiState.value = SearchUiState.Error(null)
                    return
                }
                currentPage = data.page
                totalPages = data.pages
                _hasMore.value = currentPage < totalPages
                _isSearched.value = true
                _posts.value = if (isAppend) {
                    (_posts.value + data.feed).distinctBy { it.id }
                } else {
                    data.feed
                }
                _uiState.value = if (_posts.value.isEmpty()) SearchUiState.Empty else SearchUiState.Success
            }

            is ApiResult.Error -> if (!isAppend) _uiState.value = SearchUiState.Error(result.message)
            is ApiResult.NetworkError -> if (!isAppend) {
                _uiState.value = SearchUiState.Error("网络异常，请检查网络连接")
            }
        }
    }

    private suspend fun loadUsers(q: String, page: Int, isAppend: Boolean) {
        when (val result = safeApiCall { RetroV2Client.apiService.searchUsers(q, page, pageSize) }) {
            is ApiResult.Success -> {
                val data = result.data
                if (data == null) {
                    if (!isAppend) _uiState.value = SearchUiState.Error(null)
                    return
                }
                currentPage = data.page
                totalPages = data.pages
                _hasMore.value = currentPage < totalPages
                _isSearched.value = true
                _users.value = if (isAppend) {
                    (_users.value + data.userList).distinctBy { it.uid }
                } else {
                    data.userList
                }
                _uiState.value = if (_users.value.isEmpty()) SearchUiState.Empty else SearchUiState.Success
            }

            is ApiResult.Error -> if (!isAppend) _uiState.value = SearchUiState.Error(result.message)
            is ApiResult.NetworkError -> if (!isAppend) {
                _uiState.value = SearchUiState.Error("网络异常，请检查网络连接")
            }
        }
    }

    /** 帖子点赞：乐观更新 + 失败回滚 */
    fun toggleLike(postId: Int) {
        val list = _posts.value
        val index = list.indexOfFirst { it.id == postId }
        if (index == -1) return

        val target = list[index]
        val willLike = !target.isLiked
        val newCount = if (willLike) {
            target.likeCount + 1
        } else {
            (target.likeCount - 1).coerceAtLeast(0)
        }

        _posts.value = list.toMutableList().apply {
            this[index] = target.copy(isLiked = willLike, likeCount = newCount)
        }

        viewModelScope.launch {
            when (val result = safeApiCall { RetroV2Client.apiService.likePost(postId) }) {
                is ApiResult.Success -> {
                    result.data?.let { likeData ->
                        val current = _posts.value
                        val i = current.indexOfFirst { it.id == postId }
                        if (i != -1) {
                            _posts.value = current.toMutableList().apply {
                                this[i] = this[i].copy(
                                    isLiked = likeData.isLiked,
                                    likeCount = likeData.likeCount
                                )
                            }
                        }
                    }
                }

                else -> {
                    val current = _posts.value
                    val i = current.indexOfFirst { it.id == postId }
                    if (i != -1) {
                        _posts.value = current.toMutableList().apply { this[i] = target }
                    }
                    Log.d("SEARCH VM", "toggleLike 失败，已回滚 id=$postId")
                }
            }
        }
    }
}

sealed class SearchUiState {
    /** 还没开始搜（输入为空） */
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    object Success : SearchUiState()
    object Empty : SearchUiState()
    class Error(val message: String? = null) : SearchUiState()
}