package com.freewdcmkt.bck.layout.ui.community

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.Link.feedLink
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.ReplyCard
import com.freewdcmkt.bck.components.ReplyInputBar
import com.freewdcmkt.bck.components.freewd.ContentMarkdown
import com.freewdcmkt.bck.components.freewd.ContentText
import com.freewdcmkt.bck.components.freewd.DateText
import com.freewdcmkt.bck.components.freewd.FreewdDialog
import com.freewdcmkt.bck.components.freewd.IconTextButton
import com.freewdcmkt.bck.components.freewd.ImageCard
import com.freewdcmkt.bck.components.freewd.TitleText
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.viewmodel.FeedDetailUiState
import com.freewdcmkt.bck.viewmodel.FeedDetailViewmodel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailLayout(
    id: Int,
    //zone: Int,
    viewmodel: FeedDetailViewmodel = viewModel(),
    onDeleteFeed: () -> Unit,
    onBack: () -> Unit,
    onToPreviewImg: (String) -> Unit
) {
    val uiState by viewmodel.feedDetailUiState.collectAsState()
    val isAuthor by viewmodel.isAuthor.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedHint = stringResource(R.string.copy_feed_link_hint)
    val isExpanded = remember { mutableStateOf(false) }
    val isShowDialog = rememberSaveable() { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val replyQq = rememberSaveable() { mutableStateOf("") }
    val replyUsername = rememberSaveable() { mutableStateOf("") }

    val context = LocalContext.current

    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    LaunchedEffect(id) { viewmodel.fetchData(id,true) }
    if (isShowDialog.value) {
        FreewdDialog(
            onDismiss = { isShowDialog.value = false },
            onConfirm = {
                viewmodel.deleteFeed(id)
                isShowDialog.value = false
            },
            title = stringResource(R.string.delete_post_title_hint),
            msg = stringResource(R.string.delete_post_message_hint),
            hintMsg1 = stringResource(R.string.no_hint),
            hintMsg2 = stringResource(R.string.yes_hint)
        )
    }
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
                        DropdownMenuItem(
                            { Text(stringResource(R.string.copy_feed_link_hint)) },
                            onClick = {
                                val link = feedLink(id)
                                val clip = ClipData.newPlainText("Feed link", link)

                                clipboardManager.setPrimaryClip(clip)
                                isExpanded.value = false
                                scope.launch {
                                    snackBarHostState.showSnackbar(
                                        message = copiedHint,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                        )
                        if (isAuthor) DropdownMenuItem(
                            text = {
                                Text(
                                    stringResource(R.string.delete_hint),
                                    color = Color.Red
                                )
                            },
                            onClick = {
                                isShowDialog.value = true
                                isExpanded.value = false
                            },
                        )

                    }
                })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            if (uiState is FeedDetailUiState.Success) ReplyInputBar(
                username = replyUsername.value,
                onSend = {
                    val targetQq = replyQq.value
                    viewmodel.replyFeed(id, it, targetQq.ifEmpty { null })
                    focusRequester.requestFocus()
                    replyQq.value = ""
                },
                modifier = Modifier.imePadding(),
                focusRequester = focusRequester
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            when (uiState) {
                is FeedDetailUiState.Loading -> LoadingCard()
                is FeedDetailUiState.Error -> {
                    LoadErrorUiLayout(
                        onClick = { viewmodel.fetchData(id) },
                        msg = (uiState as FeedDetailUiState.Error).msg,
                    )
                }

                is FeedDetailUiState.DeleteSuccess -> {
                    onDeleteFeed()
                }

                is FeedDetailUiState.Success -> {
                    replyUsername.value =
                        (uiState as FeedDetailUiState.Success).feedDetailData.username
                    FeedUiLayout(
                        (uiState as FeedDetailUiState.Success).feedDetailData,
                        onClickLike = {
                            viewmodel.seedLike(
                                id,
                                (uiState as FeedDetailUiState.Success).feedDetailData.isLiked
                            )
                            Log.d("FEED DETAIL LAYOUT", "ONCLICKLIKEBUTTON")
                        }, onReplyUser = { qq, username ->
                            focusRequester.requestFocus()
                            replyQq.value = qq
                            replyUsername.value = username
                        }, onToPreviewImg = onToPreviewImg)
                }
            }
        }
    }
}

@Composable
private fun FeedUiLayout(
    feedDetailData: FeedDetailData,
    onClickLike: () -> Unit,
    onReplyUser: (String, String) -> Unit,
    onToPreviewImg: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                            )
                        }
                    }

                    // ===== 内容区域 (增加间距) =====
                    Spacer(modifier = Modifier.height(10.dp))
                    SelectionContainer {
                        Column {
                            if (feedDetailData.title != null) {
                                TitleText(
                                    feedDetailData.title,
                                )
                            }
                            if (feedDetailData.isMarkdown && feedDetailData.msg != null) {
                                ContentMarkdown(
                                    feedDetailData.msg,
                                )
                            } else if (feedDetailData.msg != null) {
                                ContentText(
                                    text = feedDetailData.msg
                                )
                            }
                            if (feedDetailData.img != null) ImageCard(
                                feedDetailData.img,
                                onClick = onToPreviewImg
                            )
                        }

                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        IconTextButton(
                            icon = if (feedDetailData.isLiked)
                                R.drawable.baseline_favorite_24
                            else
                                R.drawable.baseline_favorite_border_24,
                            description = stringResource(R.string.favorite_hint),
                            text = feedDetailData.likeCount.toString(),
                            onClick = onClickLike,
                        )
                    }
                }
            }
        }
        items(
            items = feedDetailData.reply ?: emptyList(),
            key = { " ${it.commentId}_${it.date}" }
        ) { replyData ->
            ReplyCard(
                replyData,
                onReplyUser = onReplyUser
            )
        }
    }


}





