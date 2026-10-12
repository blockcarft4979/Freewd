package com.freewdcmkt.bck.layout.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.FeedCard
import com.freewdcmkt.bck.components.freewd.FreewdSearchBar
import com.freewdcmkt.bck.components.freewd.UidText
import com.freewdcmkt.bck.components.freewd.UserIcon
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.data.screen.PostsData
import com.freewdcmkt.bck.data.screen.SearchMode
import com.freewdcmkt.bck.data.screen.SearchUserData
import com.freewdcmkt.bck.viewmodel.community.SearchUiState
import com.freewdcmkt.bck.viewmodel.community.SearchViewmodel
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchLayout(
    onToFeedDetail: (id: Int) -> Unit,
    onToUserProfile: (qq: String) -> Unit,
    onBack: () -> Unit
) {
    val viewmodel: SearchViewmodel = viewModel()
    val uiState by viewmodel.uiState.collectAsState()
    val mode by viewmodel.mode.collectAsState()
    val posts by viewmodel.posts.collectAsState()
    val users by viewmodel.users.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()
    val isSearched by viewmodel.isSearched.collectAsState()

    val keyboard = LocalSoftwareKeyboardController.current
    // 帖子与用户各自独立的列表状态，切换模式不互相干扰滚动位置
    val postListState = rememberLazyListState()
    val userListState = rememberLazyListState()

    var input by rememberSaveable { mutableStateOf("") }
    var imgUrl by remember { mutableStateOf<String?>(null) }

    // 滑到底自动加载下一页（切模式时会用新模式的列表状态重新订阅）
    val activeListState = if (mode == SearchMode.POST) postListState else userListState
    LaunchedEffect(activeListState, hasMore, mode) {
        snapshotFlow {
            val lastVisible = activeListState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = activeListState.layoutInfo.totalItemsCount
            total > 0 && lastVisible >= total - 1
        }
            .distinctUntilChanged()
            .collect { atEnd ->
                if (atEnd && hasMore) viewmodel.loadMore()
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    FreewdSearchBar(
                        value = input,
                        onValueChange = {
                            input = it
                           viewmodel.onKeywordChange(it)
                        },
                        hint = if (mode == SearchMode.POST) stringResource(R.string.search_hint) else stringResource(
                            R.string.search_user_hint
                        ),
                        onSearch = {
                            viewmodel.onSearchAction()
                            keyboard?.hide()
                        },
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = stringResource(R.string.back_hint)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {

            SearchModeSwitch(
                mode = mode,
                onModeChange = { viewmodel.onModeChange(it) },
                modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                when {
                    uiState is SearchUiState.Idle -> FreewdHint(
                        icon = R.drawable.baseline_search_24,
                        hint = if (mode == SearchMode.POST) stringResource(R.string.search_hint)
                        else stringResource(R.string.search_user_hint)
                    )

                    uiState is SearchUiState.Loading -> LoadingCard(
                        stringResource(R.string.searching_hint)
                    )

                    uiState is SearchUiState.Error -> FreewdHint(
                        icon = R.drawable.baseline_error_24,
                        hint = (uiState as SearchUiState.Error).message
                            ?: stringResource(R.string.unknown_error)
                    )

                    // 搜完了才判空，否则请求途中会闪一下空态
                    isSearched && posts.isEmpty() && users.isEmpty() -> FreewdHint(
                        icon = R.drawable.baseline_search_24,
                        hint = if (mode == SearchMode.POST) stringResource(R.string.search_empty_hint)
                        else stringResource(R.string.search_user_empty_hint)
                    )

                    mode == SearchMode.POST -> SearchPostsContent(
                        posts = posts,
                        listState = postListState,
                        isLoadingMore = isLoadingMore,
                        onToFeedDetail = onToFeedDetail,
                        onToPreviewImg = { imgUrl = it },
                        onLike = { viewmodel.toggleLike(it) }
                    )

                    else -> SearchUsersContent(
                        users = users,
                        listState = userListState,
                        isLoadingMore = isLoadingMore,
                        onToUserProfile = onToUserProfile
                    )
                }
            }
        }

        PreviewImgUi(url = imgUrl ?: "", onDismiss = { imgUrl = null })
    }
}

/** 「帖子 / 用户」分段切换 */
@Composable
private fun SearchModeSwitch(
    mode: SearchMode,
    onModeChange: (SearchMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        SegmentedItem(
            selected = mode == SearchMode.POST,
            label = stringResource(R.string.search_tab_post),
            onClick = { onModeChange(SearchMode.POST) }
        )
        SegmentedItem(
            selected = mode == SearchMode.USER,
            label = stringResource(R.string.search_tab_user),
            onClick = { onModeChange(SearchMode.USER) }
        )
    }
}

@Composable
private fun SegmentedItem(selected: Boolean, label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) MaterialTheme.colorScheme.surface else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }
        )
    }
}

/** 帖子结果列表 */
@Composable
private fun SearchPostsContent(
    posts: List<PostsData>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    onToFeedDetail: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit
) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
        items(items = posts, key = { it.id }) { feed ->
            FeedCard(
                feed = feed,
                onClick = onToFeedDetail,
                onToPreviewImg = onToPreviewImg,
                onLike = onLike
            )
        }
        if (isLoadingMore) {
            item { LoadingCard() }
        }
    }
}

/** 用户结果列表 */
@Composable
private fun SearchUsersContent(
    users: List<SearchUserData>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    onToUserProfile: (String) -> Unit
) {
    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(bottom = 12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = users, key = { it.uid }) { user ->
            UserResultCard(user = user, onClick = { onToUserProfile(user.qq) })
        }
        if (isLoadingMore) {
            item { LoadingCard() }
        }
    }
}

@Composable
private fun UserResultCard(user: SearchUserData, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 15.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserIcon(url = userAvatarUrl(user.qq), size = 44.dp)

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                UsernameText(user.username)
                UidText(stringResource(R.string.uid_hint, user.uid.toString()))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = stringResource(R.string.user_post_count_hint, user.postCount),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(R.string.user_xp_hint, user.xp),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 加载更多的转圈行（放在 components/ui/LoadingMoreRow.kt 里共享） */
