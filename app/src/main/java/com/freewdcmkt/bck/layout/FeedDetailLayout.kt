package com.freewdcmkt.bck.layout

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.ContentText
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.components.ReplyCard
import com.freewdcmkt.bck.components.TitleText
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.viewmodel.FeedDetailUiState
import com.freewdcmkt.bck.viewmodel.FeedDetailViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailLayout(id: String, viewmodel: FeedDetailViewmodel = viewModel()) {
    val uiState by viewmodel.feedDetailUiState.collectAsState()
    LaunchedEffect(Unit) { viewmodel.fetchData(id) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.feed_detail_hint)) }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            when (uiState) {
                is FeedDetailUiState.Loading -> LoadingCard()
                is FeedDetailUiState.Error -> LoadErrorUiLayout(onClick = { viewmodel.fetchData(id) })
                is FeedDetailUiState.Success -> UiLayout((uiState as FeedDetailUiState.Success).feedDetailData)
            }
        }
    }
}

@Composable
private fun UiLayout(feedDetailData: FeedDetailData) {
    Column {
        Row {
            Image(
                painter = rememberAsyncImagePainter(userAvatarUrl(feedDetailData.qq)),
                contentDescription = null, modifier = Modifier
                    .size(48.dp)
                    .padding(5.dp)
                    .clip(CircleShape)
            )
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = feedDetailData.username,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = feedDetailData.date,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        TitleText(feedDetailData.title)
        ContentText(feedDetailData.msg)
        LazyColumn() {
            items(items = feedDetailData.reply, key = { "${it.qq}_{${it.date}}" }) { replyData ->
                ReplyCard(replyData)
            }
        }
    }
}

@Composable
@Preview
private fun Show() {
    val mockData: FeedDetailData = FeedDetailData(
        "NIHAO HELLO",
        "This is a test content",
        "0",
        "BCK",
        "2024-02-14",
        100,
        emptyList()
    )
    UiLayout(mockData)
}



