package com.freewdcmkt.bck.layout.ui.community

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.FeedCard
import com.freewdcmkt.bck.components.freewd.FreewdFooter
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.data.screen.Feed
import com.freewdcmkt.bck.viewmodel.community.FeedListViewmodel
import com.freewdcmkt.bck.viewmodel.community.FeedUiState
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedLayout(
    viewmodel: FeedListViewmodel = viewModel(),
    isRefresh: Boolean,
    onToFeedDetail: (id: Int) -> Unit,
    onToPostFeed: () -> Unit,
    onToPreviewImg: (String) -> Unit,
) {
    val uiState by viewmodel.feedUiState.collectAsState()
    val feedListData by viewmodel.feedListData.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()
    val listState = viewmodel.listState


    val feedList = feedListData?.feed ?: emptyList()

    LaunchedEffect(isRefresh) {
        if (isRefresh) {
            listState.scrollToItem(0)
            viewmodel.fetchData(forceRefresh = true)
        }
    }
   // LaunchedEffect(isNoNetwork) { if (isNoNetwork) onNoNetWork() }
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalCount = listState.layoutInfo.totalItemsCount
            lastVisible?.index == totalCount - 1
        }.distinctUntilChanged().collect { isAtEnd ->
            if (isAtEnd) {
                Log.d("FEED LAYOUT", "AT END")
                viewmodel.loadMore()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onToPostFeed,
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_add_24),
                    contentDescription = stringResource(R.string.add_post_hint)
                )
            }
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
        ) {
            when {

                uiState is FeedUiState.Loading && feedList.isEmpty() -> LoadingCard()

                else -> {

                    FeedUiLayout(
                        feed = feedList,
                        onClick = { onToFeedDetail(it) },
                        listState = listState,
                        isLoadingMore = isLoadingMore,
                        hasMore = hasMore,
                        onToPreviewImg = onToPreviewImg
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedUiLayout(
    feed: List<Feed>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit
) {
    LazyColumn(state = listState) {
        items(
            items = feed, key = { it.id }) { feed ->
            FeedCard(
                feed, onClick = { onClick(feed.id) }, onToPreviewImg = onToPreviewImg
            )
        }
        if (isLoadingMore) {
            item {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(64.dp)
                ) { LoadingCard() }
            }
        } else if (!hasMore && feed.isNotEmpty()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp), Alignment.Center
                ) {
                    FreewdFooter()
                }
            }
        }
    }
}
