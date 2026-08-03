package com.freewdcmkt.bck.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.viewmodel.PostFeedUiState
import com.freewdcmkt.bck.viewmodel.PostFeedViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedLayout(
    zone: Int,
    onUploaded: () -> Unit,
    onBack: () -> Unit,
    viewmodel: PostFeedViewmodel = viewModel()
) {
    val uiState by viewmodel.postFeedUiState.collectAsState()
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.add_post_hint)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_24),
                        stringResource(R.string.back_hint)
                    )
                }
            })
    }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .fillMaxSize()
        ) {
            when (uiState) {
                is PostFeedUiState.NoAction -> PostFeedUiLayout(onPostFeed = { title, message ->
                    viewmodel.postFeed(
                        zone = zone,
                        title = title,
                        message = message
                    )
                })

                is PostFeedUiState.Upload -> LoadingCard()
                is PostFeedUiState.Error -> LoadErrorUiLayout(
                    onClick = { viewmodel.postFeedUiState },
                    msg = (uiState as PostFeedUiState.Error).msg,
                    buttonMsg = stringResource(R.string.retry_hint),
                    icon = painterResource(R.drawable.baseline_refresh_24)
                )

                is PostFeedUiState.Success -> onUploaded()

            }
        }
    }
}

@Composable
fun PostFeedUiLayout(onPostFeed: (title: String?, message: String) -> Unit) {
    var title by rememberSaveable() { mutableStateOf("") }
    var message by rememberSaveable() { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    // 外层 Column 填满剩余高度，并添加 imePadding 响应键盘
    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
    ) {
        // 输入区域：权重 1，占据所有剩余空间，内部可滚动
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                value = title,
                onValueChange = { newTitle -> title = newTitle },
                label = { Text(stringResource(R.string.title_hint)) },
                maxLines = 1
            )
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                value = message,

                onValueChange = { newMessage -> message = newMessage },
                label = { Text(stringResource(R.string.content_hint)) }
            )

        }

        // 按钮固定在底部
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp), // 与上方内容隔开
            onClick = { onPostFeed(title, message) },
            enabled = message.isNotEmpty()
        ) {
            Text(stringResource(R.string.post_hint))
        }
    }
}