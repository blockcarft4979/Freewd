package com.freewdcmkt.bck.data.screen

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object SearchScreen : NavKey

/**
 * 搜索结果类型（搜索页顶部的分段切换）
 */
enum class SearchMode { POST, USER }

/**
 * GET /users/search 的返回。
 * 服务端字段是 snake_case（user_list / post_count），JsonParser 配了 SnakeCase 命名策略。
 */
@Serializable
data class SearchUsersData(
    val total: Int = 0,
    val page: Int = 1,
    val pages: Int = 0,
    val userList: List<SearchUserData> = emptyList()
)

@Serializable
data class SearchUserData(
    val uid: Int,
    val qq: String,
    val username: String,
    val xp: Int = 0,
    val postCount: Int = 0,
    val joinedAt: String? = null
)
