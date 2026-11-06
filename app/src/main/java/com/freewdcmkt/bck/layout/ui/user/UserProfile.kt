package com.freewdcmkt.bck.layout.ui.user

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.FeedCard
import com.freewdcmkt.bck.components.freewd.ExpCard
import com.freewdcmkt.bck.components.freewd.LoadingCard
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.components.freewd.UserProfileHeader
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.viewmodel.user.UserProfileUiState
import com.freewdcmkt.bck.viewmodel.user.UserProfileViewmodel
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileLayout(
    isShowBackArrow: Boolean = true,
    uid: Int,
    onToFeedDetail: (id: Int) -> Unit,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit = {},
    viewmodel: UserProfileViewmodel = viewModel()
) {
    val isSelfCenter = UserInfoData.uid.collectAsState().value == uid

    val uiState by viewmodel.uiState.collectAsState()
    val profile by viewmodel.profile.collectAsState()
    val posts by viewmodel.posts.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()
    val isLoadingPosts by viewmodel.isLoadingPosts.collectAsState()
    val localUsername by UserInfoData.username.collectAsState()
    val localExp by UserInfoData.exp.collectAsState()
    val localBio by UserInfoData.bio.collectAsState()

    val listState = rememberLazyListState()
    var imgUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) { viewmodel.load(uid) }
    Log.d("user uid", uid.toString())
    LaunchedEffect(isSelfCenter, localUsername, localExp, localBio) {
        if (isSelfCenter && profile != null) {
            viewmodel.updateLocalProfile(
                newUsername = localUsername,
                newExp = localExp,
                newBio = localBio
            )
        }
    }
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

    // 🌟 核心逻辑：监听列表滚动，判断大头像是否滑出屏幕
    val density = LocalDensity.current
    // 阈值计算：Card上padding(10) + Card内padding(16) + 头像高度(48) = 74dp
    val avatarThresholdPx = with(density) { 74.dp.toPx() }

    val showTopBarAvatar by remember(profile) {
        derivedStateOf {
            if (profile == null) return@derivedStateOf false
            val firstItem = listState.layoutInfo.visibleItemsInfo.firstOrNull()
            if (firstItem == null) {
                false
            } else if (firstItem.index == 0) {
                -firstItem.offset > avatarThresholdPx
            } else {
                true
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    // 🌟 使用 AnimatedContent 实现平滑的淡入淡出切换
                    AnimatedContent(
                        targetState = showTopBarAvatar,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(250)) togetherWith fadeOut(
                                animationSpec = tween(
                                    250
                                )
                            )
                        }
                    ) { showAvatar ->
                        val currentProfile = profile
                        if (showAvatar && currentProfile != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                UserCard(
                                    userAvatarUrl(currentProfile.qq),
                                    username = currentProfile.username,
                                    uid = currentProfile.uid.toString()
                                )
                            }
                        } else {
                            // 状态二：显示默认标题
                            Text(
                                stringResource(if (isSelfCenter) R.string.user_center_hint else R.string.user_profile_hint),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (isShowBackArrow) IconButton(onClick = onBack) {
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
            PullToRefreshBox(
                isRefreshing = uiState is UserProfileUiState.Loading,
                onRefresh = { viewmodel.load(uid,true) },
            ) {
                when (uiState) {
                    is UserProfileUiState.Loading -> LoadingCard(
                        stringResource(R.string.loading_hint)
                    )

                    is UserProfileUiState.Error -> Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        FreewdHint(
                            icon = R.drawable.baseline_error_24,
                            hint = (uiState as UserProfileUiState.Error).message
                                ?: stringResource(R.string.unknown_error)
                        )
                        content()
                    }

                    else -> LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize()
                    ) {

                        profile?.let { data ->
                            item {
                                UserProfileHeader(data)
                                ExpCard(exp = data.xp)
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier,
                                content = content
                            )
                        }
                        item {
                            Text(
                                text = stringResource(
                                    if (isSelfCenter) R.string.user_my_posts_hint else R.string.user_posts_section_hint
                                ),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = if (isSelfCenter) 14.dp else 6.dp,
                                    bottom = 2.dp
                                )
                            )
                        }
                        if (isLoadingPosts) {
                            item {
                                LoadingCard(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .height(64.dp)
                                )
                            }
                        } else if (posts.isEmpty()) {
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

                            items(items = posts, key = { it.id }) { feed ->
                                FeedCard(
                                    feed = feed,
                                    onClick = onToFeedDetail,
                                    onToPreviewImg = { imgUrl = it },
                                    onLike = { viewmodel.toggleLike(it) }
                                )
                            }

                            if (isLoadingMore) {
                                item {
                                    LoadingCard(
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .height(64.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }


            PreviewImgUi(url = imgUrl ?: "", onDismiss = { imgUrl = null })
        }
    }
}

/** 顶部资料卡（用户主页 / 个人中心共用） */
