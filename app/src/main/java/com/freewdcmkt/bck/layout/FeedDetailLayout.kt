package com.freewdcmkt.bck.layout

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
    onDeleteFeed: () -> Unit
) {
    val uiState by viewmodel.feedDetailUiState.collectAsState()
    val isAuthor by viewmodel.isAuthor.collectAsState()
    LaunchedEffect(Unit) { viewmodel.fetchData(id, zone) }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.feed_detail_hint)) }) }) { innerPadding ->
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
                        },
                        isAuthor = isAuthor,
                        onDeleteFeed = { viewmodel.deleteFeed(id) })
                }
            }
        }
    }
}

@Composable
private fun FeedUiLayout(
    feedDetailData: FeedDetailData,
    onClickLike: () -> Unit,
    isAuthor: Boolean,
    onDeleteFeed: () -> Unit
) {

    LazyColumn {
        item {
            Column(modifier = Modifier.padding(horizontal = 15.dp)) {
                Row(

                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(userAvatarUrl(feedDetailData.qq)),
                        contentDescription = null, modifier = Modifier
                            .size(48.dp)
                            .padding(5.dp)
                            .clip(CircleShape)
                    )
                    Column(
                        modifier = Modifier.padding(start = 8.dp),
                        horizontalAlignment = Alignment.Start,
                        verticalArrangement = Arrangement.Center
                    ) {
                        UsernameText(feedDetailData.username)
                        DateText(feedDetailData.date)
                    }
                }
                if (feedDetailData.title != null) TitleText(feedDetailData.title)
                //if (feedDetailData.msg!=null)ContentText(feedDetailData.msg)
                if (feedDetailData.isMarkdown && feedDetailData.msg != null) {
                    Markdown(feedDetailData.msg)
                } else if (feedDetailData.msg != null) {
                    Text(feedDetailData.msg)
                }
                Row(
                    horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
                ) {
                    IconTextButton(
                        if (feedDetailData.isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24,
                        description = stringResource(R.string.favorite_hint),
                        text = feedDetailData.likeCount.toString(),
                        onClick = onClickLike
                    )
                    if (isAuthor) IconTextButton(
                        onClick = onDeleteFeed,
                        icon = R.drawable.baseline_delete_24,
                        description = stringResource(R.string.delete_hint),
                        text = stringResource(R.string.delete_hint)
                    )
                }
            }
        }
        items(
            items = feedDetailData.reply ?: emptyList(),
            key = { "${it.qq}_${it.date}" }) { replyData ->
            ReplyCard(replyData)
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
        }
    }
}





