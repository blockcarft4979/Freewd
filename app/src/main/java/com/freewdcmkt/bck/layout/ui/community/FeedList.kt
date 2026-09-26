package com.freewdcmkt.bck.layout.ui.community

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
fun FeedListHost(
    viewmodel: FeedListViewmodel = viewModel(),
    isRefresh: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onToFeedDetail: (id: Int) -> Unit,
    onToPostFeed: () -> Unit,
    onToPreviewImg: (String) -> Unit,
) {
    val uiState by viewmodel.feedUiState.collectAsState()
    val feedListData by viewmodel.feedListData.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()

    val listState = rememberLazyListState()

    val feedList = feedListData?.feed ?: emptyList()

    LaunchedEffect(isRefresh) {
        if (isRefresh) {
            viewmodel.fetchData(forceRefresh = true)
        }
        if (isRefresh && uiState is FeedUiState.Success) {
            listState.scrollToItem(0)
        }
    }

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

                    FeedListUi(
                        feed = feedList,
                        onClick = { onToFeedDetail(it) },
                        listState = listState,
                        isLoadingMore = isLoadingMore,
                        hasMore = hasMore,
                        onToPreviewImg = onToPreviewImg,
                        scrollBehavior = scrollBehavior
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedListUi(
    feed: List<Feed>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    scrollBehavior: TopAppBarScrollBehavior,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit
) {
    LazyColumn(state = listState, modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)) {
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
