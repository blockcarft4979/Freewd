package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.freewdcmkt.bck.components.FeedCard
import com.freewdcmkt.bck.components.freewd.ExpCard
import com.freewdcmkt.bck.components.freewd.LoadingCard
import com.freewdcmkt.bck.components.freewd.UidText
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.components.freewd.UserIcon
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.data.screen.UserProfileData
import com.freewdcmkt.bck.viewmodel.user.UserProfileUiState
import com.freewdcmkt.bck.viewmodel.user.UserProfileViewmodel
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * 用户主页 & 个人中心 (合二为一)
 *
 * @param qq 目标 QQ。**传空字符串 "" 代表查看自己（个人中心）**，此时会显示经验卡。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileLayout(
    qq: String,
    onToFeedDetail: (id: Int) -> Unit,
    onBack: () -> Unit,
    viewmodel: UserProfileViewmodel = viewModel()
) {
    // 判断是否是“个人中心”
    val isSelfCenter = qq.isBlank()

    val uiState by viewmodel.uiState.collectAsState()
    val profile by viewmodel.profile.collectAsState()
    val posts by viewmodel.posts.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()
    val isLoadingPosts by viewmodel.isLoadingPosts.collectAsState()

    val listState = rememberLazyListState()
    var imgUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(qq) { viewmodel.load(qq) }

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
                // 如果第一个可见的还是资料卡，算一下它向上滑了多少
                -firstItem.offset > avatarThresholdPx
            } else {
                // 如果第一个可见的已经不是资料卡了，说明肯定滑出去了
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

                        item {
                            Box(modifier = Modifier.padding(horizontal = 15.dp)) {
                                ExpCard(exp = data.xp)
                            }
                        }
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

            PreviewImgUi(url = imgUrl ?: "", onDismiss = { imgUrl = null })
        }
    }
}

/** 顶部资料卡（用户主页 / 个人中心共用） */
@Composable
fun UserProfileHeader(data: UserProfileData) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserIcon(url = userAvatarUrl(data.qq))

                Spacer(modifier = Modifier.size(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    UsernameText(data.username)

                    UidText(stringResource(R.string.uid_hint, data.uid.toString()))
                    data.joinedAt?.let { joined ->
                        Text(
                            text = stringResource(
                                R.string.user_joined_hint,
                                joined.substringBefore(' ')
                            ),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileStat(
                    value = data.postCount.toString(),
                    label = stringResource(R.string.user_stat_post)
                )
                ProfileStat(
                    value = data.totalLikes.toString(),
                    label = stringResource(R.string.user_stat_like)
                )
                ProfileStat(
                    value = data.xp.toString(),
                    label = stringResource(R.string.user_stat_xp)
                )
            }
        }
    }
}

@Composable
fun ProfileStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}