package com.freewdcmkt.bck.layout.ui.community

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.Link.feedLink
import com.freewdcmkt.bck.components.freewd.FeedDetailCard
import com.freewdcmkt.bck.components.freewd.FreewdLoadingDialog
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.LoadingCard
import com.freewdcmkt.bck.components.freewd.ReplyCard
import com.freewdcmkt.bck.components.freewd.ReplyInputBar
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.viewmodel.community.FeedDetailEvent
import com.freewdcmkt.bck.viewmodel.community.FeedDetailUiState
import com.freewdcmkt.bck.viewmodel.community.FeedDetailViewmodel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailLayout(
    id: Int,
    onDeleteFeed: () -> Unit,
    onBack: () -> Unit,
    onToUserProfile: (uid: Int) -> Unit,
) {
    val viewmodel: FeedDetailViewmodel = viewModel()
    val feedDetailData by viewmodel.feedDetailData.collectAsState()
    val uiState by viewmodel.feedDetailUiState.collectAsState()
    val isAuthor by viewmodel.isAuthor.collectAsState()
    val errorMSg by viewmodel.errorMsg.collectAsState()
    val isNoNetwork by viewmodel.isNoNetwork.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val deletingIds = remember { mutableStateListOf<Int>() }

    val pendingDeleteRid = rememberSaveable { mutableStateOf<Int?>(null) }
    val copiedHint = stringResource(R.string.copied_link_hint)
    val isExpanded = remember { mutableStateOf(false) }
    val isShowDialog = rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val replyQq = rememberSaveable { mutableStateOf("") }
    val replyUsername = rememberSaveable { mutableStateOf("") }
    val isSendingReply by viewmodel.isSendingReply.collectAsState()
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    LaunchedEffect(id) { viewmodel.fetchData(id, false) }
    if (isSendingReply) {
        FreewdLoadingDialog(stringResource(R.string.submitting_hint))
    }
    LaunchedEffect(feedDetailData.username) {
        replyUsername.value = feedDetailData.username
    }
    LaunchedEffect(uiState) {
        if (uiState is FeedDetailUiState.DeleteSuccess) {
            viewmodel.resetUi()
            onDeleteFeed()
        }
    }

    // === 收 ViewModel 一次性事件 → snackbar ===
    LaunchedEffect(Unit) {
        viewmodel.events.collect { event ->
            val text = when (event) {
                is FeedDetailEvent.ShowError ->
                    event.serverMessage?.takeIf { it.isNotBlank() }
                        ?: context.getString(event.messageRes)

                is FeedDetailEvent.ShowMessage ->
                    context.getString(event.messageRes)
            }
            snackBarHostState.showSnackbar(
                message = text,
                duration = SnackbarDuration.Short,
            )
        }
    }

    if (isShowDialog.value) {
        FreewdModalBottomSheet(
            onDismiss = { isShowDialog.value = false },
            onConfirm = {
                viewmodel.deleteFeed(id)
                isShowDialog.value = false
            },
            title = stringResource(R.string.delete_post_title_hint),
            msg = stringResource(R.string.delete_post_message_hint),
            stringResource(R.string.no_hint),
            stringResource(R.string.yes_hint)
        )
    }

    if (pendingDeleteRid.value != null) {
        FreewdModalBottomSheet(
            onDismiss = { pendingDeleteRid.value = null },
            onConfirm = {
                pendingDeleteRid.value?.let { rid ->
                    deletingIds.add(rid)
                    scope.launch {
                        delay(320)
                        viewmodel.deleteReply(id, rid)
                        deletingIds.remove(rid)
                    }
                }
                pendingDeleteRid.value = null
            },
            title = stringResource(R.string.delete_comment_title_hint),
            msg = stringResource(R.string.delete_comment_message_hint),
            stringResource(R.string.no_hint),
            stringResource(R.string.yes_hint)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                modifier = Modifier,
                navigationIcon = {
                    IconButton(onBack) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = stringResource(R.string.back_hint)
                        )
                    }
                },
                title = {
                    Text(stringResource(R.string.feed_detail_hint))
                },
                actions = {
                    if (uiState is FeedDetailUiState.Success) IconButton(onClick = {
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
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            if (uiState is FeedDetailUiState.Success)
                ReplyInputBar(
                    username = replyUsername.value,
                    onSend = { text ->
                        viewmodel.replyFeed(
                            id = id,
                            content = text,
                            replyQq = replyQq.value.ifEmpty { null },
                        )
                        // 重置回复目标，输入栏占位回到楼主
                        replyQq.value = ""
                        replyUsername.value = feedDetailData.username
                        focusRequester.requestFocus()
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

                is FeedDetailUiState.Error -> FreewdHint(
                    hint = if (isNoNetwork) stringResource(R.string.unknown_error) else errorMSg,
                    icon = R.drawable.baseline_close_24
                )

                else -> {
                    FeedUiLayout(
                        feedDetailData,
                        deletingIds = deletingIds,
                        onClickLike = {
                            viewmodel.seedLike(
                                id,
                                feedDetailData.isLiked
                            )
                        },
                        onReplyUser = { qq, username ->
                            focusRequester.requestFocus()
                            replyQq.value = qq
                            replyUsername.value = username
                        },
                        onDeleteReply = { pendingDeleteRid.value = it },
                        onToUserProfile = onToUserProfile,
                        onAddComment = { focusRequester.requestFocus() }
                    )
                }
            }
        }

    }
}


@Composable
private fun FeedUiLayout(
    feedDetailData: FeedDetailData,
    deletingIds: List<Int>,
    onClickLike: (id: Int) -> Unit,
    onReplyUser: (String, String) -> Unit,
    onDeleteReply: (Int) -> Unit,
    onToUserProfile: (uid: Int) -> Unit,
    onAddComment: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            FeedDetailCard(
                feedDetailData,
                onLike = onClickLike,
                onClickUserIcon = { onToUserProfile(feedDetailData.uid) }
            )
            Text(stringResource(R.string.reply_hint), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(horizontal = 15.dp))
            if (feedDetailData.reply?.isEmpty() == true) EmptyReplyComponent(onClick = onAddComment)
        }

        items(
            items = feedDetailData.reply ?: emptyList(),
            key = { "${it.commentId}_${it.date}" }
        ) { replyData ->

            ReplyCard(
                isDeleting = deletingIds.contains(replyData.commentId),
                replyData = replyData,
                onReplyUser = onReplyUser,
                onLongClick = {
                    if (replyData.canDelete) {
                        onDeleteReply(replyData.commentId)
                    }
                },
                onToUserProfile = onToUserProfile
            )
        }
    }
}

@Composable
private fun EmptyReplyComponent(
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // 给个柔和的背景色
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally, // 居中好看点
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.add_comment_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = stringResource(R.string.reply_hint),
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}