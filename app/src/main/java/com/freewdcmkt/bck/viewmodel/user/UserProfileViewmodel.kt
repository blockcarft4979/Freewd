package com.freewdcmkt.bck.viewmodel.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.PostsData
import com.freewdcmkt.bck.data.screen.UserProfileData
import com.freewdcmkt.bck.util.UserInfoManager
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
 * - 资料：GET /users/{uid}
 * - TA 的帖子：GET /search?uid={uid}（只传 uid 不传 q = 该用户全部帖子，按时间倒序）
 *
 * 全链路以 [uid] 为主键：与 qq 解耦，避免 QQ 作为业务标识导致的越权/枚举风险。
 * 匿名帖子由后端统一输出 uid = 0、qq = "0"，前端据此短路跳转即可。
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

    private val _isLoadingPosts = MutableStateFlow(false)
    val isLoadingPosts: StateFlow<Boolean> = _isLoadingPosts.asStateFlow()

    /** 当前已加载的目标 uid；null 表示还没加载过。uid 恒 >= 1000，不会出现 0 的合法值 */
    private var loadedTargetUid: Int? = null
    private var currentPage = 0
    private var totalPages = 0
    private val pageSize = 20

    /**
     * @param uid 目标用户 uid（唯一主键）；由调用方（个人中心/帖子详情）传入。
     *   服务端 GET /users/{uid} 对不存在 uid 返回 404，由 [safeApiCall] 统一转换为 Error。
     */
    fun load(uid: Int, forceRefresh: Boolean = false) {
        if (uid <= 0) {
            _uiState.value = UserProfileUiState.Error("无效的用户 UID")
            return
        }
        if (loadedTargetUid == uid && _uiState.value is UserProfileUiState.Success && !forceRefresh) {
            return
        }

        loadedTargetUid = uid
        currentPage = 0
        totalPages = 0
        _hasMore.value = false
        _isLoadingMore.value = false
        _isLoadingPosts.value = false
        _uiState.value = UserProfileUiState.Loading
        _posts.value = emptyList()

        viewModelScope.launch {
            when (val result =
                safeApiCall { RetroV2Client.apiService.getUserProfile(uid) }) {
                is ApiResult.Success -> {
                    val data = result.data
                    if (data == null) {
                        _uiState.value = UserProfileUiState.Error(null)
                        return@launch
                    }
                    _profile.value = data
                    _uiState.value = UserProfileUiState.Success
                    if (data.qq == UserInfoData.account.value) {
                        UserInfoManager.saveUserBio(data.bio)
                        UserInfoManager.saveLastCheckInDate(data.lastCheckinDate ?: "")
                        UserInfoManager.saveExp(data.xp)
                        UserInfoManager.saveCheckInDays(data.checkInDays)
                    }
                    loadPosts(targetUid = uid, page = 1, isAppend = false)
                }

                is ApiResult.Error -> {
                    _uiState.value = UserProfileUiState.Error(result.message)
                }

                is ApiResult.NetworkError -> {
                    _uiState.value = UserProfileUiState.Error()
                }
            }
        }
    }

    fun loadMore() {
        if (!_hasMore.value || _isLoadingMore.value) return
        if (_uiState.value !is UserProfileUiState.Success) return

        val targetUid = loadedTargetUid ?: return
        if (targetUid <= 0) return

        _isLoadingMore.value = true
        viewModelScope.launch {
            loadPosts(targetUid = targetUid, page = currentPage + 1, isAppend = true)
        }
    }

    private suspend fun loadPosts(targetUid: Int, page: Int, isAppend: Boolean) {
        if (!isAppend) {
            _isLoadingPosts.value = true
        }

        try {
            // 只传 uid（其余为 null）即「该用户全部帖子」，后端按 created_at DESC 排序
            when (val result = safeApiCall {
                RetroV2Client.apiService.searchPosts(
                    query = null,
                    page = page,
                    limit = pageSize,
                    authorUid = targetUid
                )
            }) {
                is ApiResult.Success -> {
                    val data = result.data ?: return
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
            if (!isAppend) {
                _isLoadingPosts.value = false
            }
            _isLoadingMore.value = false
        }
    }

    /**
     * 本地无感更新数据（用于签到、改名等已通过其他接口完成的操作）
     *
     * 用 uid 判定归属：匿名帖 uid = 0，不会被误改；且与 qq 的字符串比较解耦。
     */
    fun updateLocalProfile(newUsername: String, newExp: Int, newBio: String) {
        val myUid = _profile.value?.uid ?: 0

        // 1. 更新资料卡数据
        _profile.value = _profile.value?.copy(
            username = newUsername,
            xp = newExp,
            bio = newBio
        )

        // 2. 更新自己帖子列表里的用户名（仅限非匿名且 uid 匹配）
        if (myUid > 0) {
            _posts.value = _posts.value.map { post ->
                if (post.uid != 0 && post.uid == myUid) {
                    post.copy(username = newUsername)
                } else {
                    post
                }
            }
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