package com.freewdcmkt.bck.layout

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.DateText
import com.freewdcmkt.bck.components.IconTextButton
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.components.ReplyCard
import com.freewdcmkt.bck.components.TitleText
import com.freewdcmkt.bck.components.UsernameText
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.viewmodel.FeedDetailUiState
import com.freewdcmkt.bck.viewmodel.FeedDetailViewmodel
import com.mikepenz.markdown.m3.Markdown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailLayout(
    id: Int,
    zone: Int,
    viewmodel: FeedDetailViewmodel = viewModel(),
    onDeleteFeed: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewmodel.feedDetailUiState.collectAsState()
    val isAuthor by viewmodel.isAuthor.collectAsState()
    val isExpanded = rememberSaveable() { mutableStateOf(false) }
    LaunchedEffect(Unit) { viewmodel.fetchData(id, zone) }
    Scaffold(

        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = stringResource(R.string.back_hint)
                        )
                    }
                },
                title = { Text(stringResource(R.string.feed_detail_hint)) },
                actions = {
                    IconButton(onClick = {
                        isExpanded.value = true
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_more_vert_24),
                            contentDescription = stringResource(R.string.more_hint)
                        )
                    }
                    DropdownMenu(
                        expanded = isExpanded.value,
                        onDismissRequest = { isExpanded.value = false },
                    ) {
                        if (isAuthor) IconTextButton(
                            icon = R.drawable.baseline_delete_24,
                            description = stringResource(R.string.delete_hint),
                            text = stringResource(R.string.delete_hint),
                            onClick = { viewmodel.deleteFeed(id) },
                        )
                    }
                })
        },

        floatingActionButton = {
            FloatingActionButton(onClick = {}) {
                Icon(
                    painter = painterResource(
                        R.drawable.baseline_reply_24
                    ), stringResource(R.string.retry_hint)
                )
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            when (uiState) {
                is FeedDetailUiState.Loading -> LoadingCard()
                is FeedDetailUiState.Error -> {
                    LoadErrorUiLayout(
                        onClick = { viewmodel.fetchData(id, zone) },
                        msg = (uiState as FeedDetailUiState.Error).msg,
                        buttonMsg = stringResource(R.string.retry_hint),
                        icon = painterResource(R.drawable.baseline_refresh_24)
                    )
                }

                is FeedDetailUiState.DeleteSuccess -> {
                    onDeleteFeed()
                }

                is FeedDetailUiState.Success -> {
                    FeedUiLayout(
                        (uiState as FeedDetailUiState.Success).feedDetailData,
                        onClickLike = {
                            viewmodel.seedLike(
                                id,
                                zone,
                                (uiState as FeedDetailUiState.Success).feedDetailData.isLiked
                            )
                            Log.d("FEED DETAIL LAYOUT", "ONCLICKLIKEBUTTON")
                        })
                }
            }
        }
    }
}

@Composable
private fun FeedUiLayout(
    feedDetailData: FeedDetailData,
    onClickLike: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)) // 1. 增加极浅的灰底，让卡片更立体
    ) {
        item {
            Card(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(userAvatarUrl(feedDetailData.qq)),
                            contentDescription = null,
                            modifier = Modifier
                                .size(44.dp) // 稍微缩小，更精致
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) // 加载占位背景
                        )
                        Column(
                            modifier = Modifier
                                .padding(start = 10.dp)
                                .weight(1f), // 3. 加weight，防止用户名过长挤占右边按钮空间
                            horizontalAlignment = Alignment.Start,
                            verticalArrangement = Arrangement.Center
                        ) {
                            UsernameText(
                                feedDetailData.username,
                                // style = MaterialTheme.typography.titleMedium // 建议加粗
                            )
                            DateText(
                                feedDetailData.date,
                                //style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }

                    // ===== 内容区域 (增加间距) =====
                    Spacer(modifier = Modifier.height(10.dp))

                    if (feedDetailData.title != null) {
                        TitleText(
                            feedDetailData.title,
                            //style = MaterialTheme.typography.titleLarge,
                            // modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    if (feedDetailData.isMarkdown && feedDetailData.msg != null) {
                        Markdown(
                            feedDetailData.msg,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else if (feedDetailData.msg != null) {
                        Text(
                            text = feedDetailData.msg,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp, // 4. 增加行高，阅读更舒适
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    // ===== 底部操作栏 (增加点击区域和动效) =====
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp) // 与内容拉开距离
                    ) {
                        // 5. 优化点赞按钮：增加内边距、点击反馈（水波纹）、动画缩放
                        IconTextButton(
                            icon = if (feedDetailData.isLiked)
                                R.drawable.baseline_favorite_24
                            else
                                R.drawable.baseline_favorite_border_24,
                            description = stringResource(R.string.favorite_hint),
                            text = feedDetailData.likeCount.toString(),
                            onClick = onClickLike,
                            modifier = Modifier
                                .animateContentSize(),
                        )
                    }
                }
            }
        }

        items(
            items = feedDetailData.reply ?: emptyList(),
            key = { "${it.qq}_${it.date}" }
        ) { replyData ->
            ReplyCard(
                replyData,
            )
        }
    }
}





