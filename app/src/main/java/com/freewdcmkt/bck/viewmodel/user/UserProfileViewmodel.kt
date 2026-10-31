package com.freewdcmkt.bck.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.screen.PostsData
import com.freewdcmkt.bck.data.screen.UserProfileData
import com.freewdcmkt.bck.util.network.ApiResult
import com.freewdcmkt.bck.util.network.RetroV2Client
import com.freewdcmkt.bck.util.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 用户主页
 *
 * - 资料：GET /users/{qq}
 * - TA 的帖子：GET /search?qq={qq}（只传 qq 不传 q = 该用户全部帖子，按时间倒序）
 */
class UserProfileViewmodel : ViewModel() {

    private val _uiState = MutableStateFlow<UserProfileUiState>(UserProfileUiState.Loading)
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    private val _profile = MutableStateFlow<UserProfileData?>(null)
    val profile: StateFlow<UserProfileData?> = _profile.asStateFlow()

    private val _posts = MutableStateFlow<List<PostsData>>(emptyList())
    val posts: StateFlow<List<PostsData>> = _posts.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(false)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    // 🌟 新增：首次加载帖子的 Loading 状态
    private val _isLoadingPosts = MutableStateFlow(false)
    val isLoadingPosts: StateFlow<Boolean> = _isLoadingPosts.asStateFlow()

    /** 当前已加载的目标（"" 代表当前登录用户）；null 表示还没加载过 */
    private var loadedTarget: String? = null
    private var currentPage = 0
    private var totalPages = 0
    private val pageSize = 20

    /**
     * @param targetQq 目标 QQ；**空串表示当前登录用户**（个人中心用）。
     *   服务端 GET /users/{qq} 在 qq 为空或为 "me" 时走 token 取自己。
     */
    fun load(targetQq: String) {
        if (loadedTarget != null && loadedTarget == targetQq &&
            _uiState.value is UserProfileUiState.Success
        ) {
            return
        }
        loadedTarget = targetQq
        currentPage = 0
        totalPages = 0
        _hasMore.value = false
        _isLoadingMore.value = false
        _isLoadingPosts.value = false // 🌟 重置状态
        _uiState.value = UserProfileUiState.Loading
        _posts.value = emptyList()

        viewModelScope.launch {
            // 看自己时走 /users/me。
            // 注意不能传空串：路由是 /users/{qq}，正则 (?P<qq>[^/]+) 要求至少一个字符，
            // GET /users/ 会直接 404「接口不存在」。服务端 show() 里对 'me' 有专门分支。
            val pathTarget = targetQq.ifBlank { "me" }

            // 先取资料，资料失败就没必要再拉帖子
            when (val result = safeApiCall { RetroV2Client.apiService.getUserProfile(pathTarget) }) {
                is ApiResult.Success -> {
                    val data = result.data
                    if (data == null) {
                        _uiState.value = UserProfileUiState.Error(null)
                        return@launch
                    }
                    _profile.value = data
                    _uiState.value = UserProfileUiState.Success
                    // 用资料里返回的 qq 拉帖子：个人中心传 "me" 时，这样也能拿到自己的 QQ
                    loadPosts(target = data.qq.ifBlank { targetQq }, page = 1, isAppend = false)
                }

                is ApiResult.Error -> {
                    _uiState.value = UserProfileUiState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    _uiState.value = UserProfileUiState.Error("网络异常，请检查网络连接")
                }
            }
        }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value) return
        if (_uiState.value !is UserProfileUiState.Success) return
        _isLoadingMore.value = true
        viewModelScope.launch {
            // 帖子一定要按真实 QQ 查；_profile 里已经带了服务端返回的 qq
            val target = _profile.value?.qq?.ifBlank { null }
                ?: loadedTarget?.takeIf { it.isNotBlank() }
                ?: return@launch
            loadPosts(target = target, page = currentPage + 1, isAppend = true)
        }
    }

    private suspend fun loadPosts(target: String, page: Int, isAppend: Boolean) {
        // 🌟 如果是加载第一页，开启帖子 Loading 状态
        if (!isAppend) {
            _isLoadingPosts.value = true
        }

        try {
            // 只传 qq（query 传 null）即「该用户的全部帖子」
            when (val result = safeApiCall {
                RetroV2Client.apiService.searchPosts(
                    query = null,
                    page = page,
                    limit = pageSize,
                    authorQq = target
                )
            }) {
                is ApiResult.Success -> {
                    val data = result.data
                    if (data == null) {
                        return // finally 会负责关闭 loading
                    }
                    currentPage = data.page
                    totalPages = data.pages
                    _hasMore.value = currentPage < totalPages
                    _posts.value = if (isAppend) {
                        (_posts.value + data.feed).distinctBy { it.id }
                    } else {
                        data.feed
                    }
                }

                is ApiResult.Error -> {
                    Log.d("USER PROFILE", "加载帖子失败: ${result.message}")
                }

                is ApiResult.NetworkError -> {
                    Log.d("USER PROFILE", "加载帖子网络异常")
                }
            }
        } finally {
            // 🌟 无论成功、失败还是异常，都确保重置状态，防止永远转圈
            if (!isAppend) {
                _isLoadingPosts.value = false
            }
            _isLoadingMore.value = false
        }
    }

    /** 点赞：乐观更新 + 失败回滚 */
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
                }
            }
        }
    }
}

sealed class UserProfileUiState {
    object Loading : UserProfileUiState()
    object Success : UserProfileUiState()
    class Error(val message: String? = null) : UserProfileUiState()
}