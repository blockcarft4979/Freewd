package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.FeedCard
import com.freewdcmkt.bck.components.freewd.ExpCard
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.viewmodel.user.UserProfileUiState
import com.freewdcmkt.bck.viewmodel.user.UserProfileViewmodel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 个人中心：看自己的主页。
 *
 * 版式与「用户主页」一致（同一套 [UserProfileHeader] 资料卡 + 帖子列表），
 * 额外多一张等级经验卡 [ExpCard]（和「我的」页面里那张是同一个组件）。
 * 资料接口 `GET /users/{qq}` 在 qq 传空时按 token 取当前登录用户。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCenterLayout(
    onToFeedDetail: (id: Int) -> Unit,
    onBack: () -> Unit,
    viewmodel: UserProfileViewmodel = viewModel()
) {
    val uiState by viewmodel.uiState.collectAsState()
    val profile by viewmodel.profile.collectAsState()
    val posts by viewmodel.posts.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()

    val listState = rememberLazyListState()
    var imgUrl by remember { mutableStateOf<String?>(null) }

    // 空串 = 当前登录用户
    LaunchedEffect(Unit) { viewmodel.load("") }

    LaunchedEffect(listState, hasMore) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
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
                title = { Text(stringResource(R.string.user_center_hint)) },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when (uiState) {
                is UserProfileUiState.Loading -> LoadingCard(
                    stringResource(R.string.loading_hint)
                )

                is UserProfileUiState.Error -> FreewdHint(
                    icon = R.drawable.baseline_error_24,
                    hint = (uiState as UserProfileUiState.Error).message
                        ?: stringResource(R.string.unknown_error)
                )

                else -> LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize()
                ) {
                    profile?.let { data ->
                        item { UserProfileHeader(data) }

                        // 等级经验卡（与「我的」页面同一个组件）
                        item {
                            Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                                ExpCard(exp = data.xp)
                            }
                        }
                    }

                    if (posts.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = stringResource(R.string.user_no_post_hint),
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.user_my_posts_hint),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 14.dp,
                                    bottom = 2.dp
                                )
                            )
                        }

                        items(items = posts, key = { it.id }) { feed ->
                            FeedCard(
                                feed = feed,
                                onClick = onToFeedDetail,
                                onToPreviewImg = { imgUrl = it },
                                onLike = { viewmodel.toggleLike(it) }
                            )
                        }

                        if (isLoadingMore) {
                            item { LoadingCard() }
                        }
                    }
                }
            }

            PreviewImgUi(url = imgUrl ?: "", onDismiss = { imgUrl = null })
        }
    }
}
