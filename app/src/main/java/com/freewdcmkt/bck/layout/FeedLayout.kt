package com.freewdcmkt.bck.layout

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.data.screen.Feed
import com.freewdcmkt.bck.util.FeedEvent
import com.freewdcmkt.bck.viewmodel.FeedUiState
import com.freewdcmkt.bck.viewmodel.FeedViewmodel
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedLayout(
    viewmodel: FeedViewmodel = viewModel(),
    zone: Int,
    onToFeedDetail: (id: Int, zone: Int) -> Unit,
    onToPostFeed: (id: Int?, zone: Int) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewmodel.feedUiState.collectAsState()
    val listState = viewmodel.listState
    LaunchedEffect(zone) { viewmodel.fetchData(zone) }
    LaunchedEffect(Unit) {
        var lastVersion = 0
        FeedEvent.refreshEvents.collect { version ->
            if (version != lastVersion) {
                lastVersion = version
                if (version > 0) {
                    Log.d("FeedLayout", "版本变化，刷新列表")
                    viewmodel.fetchData(zone, forceRefresh = true)
                }
            }
        }
    }
    LaunchedEffect(listState) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            val totalCount = listState.layoutInfo.totalItemsCount
            lastVisible?.index == totalCount - 1
        }
            .distinctUntilChanged()
            .collect { isAtEnd ->
                if (isAtEnd) {
                    Log.d("FEED LAYOUT", "AT END")
                    viewmodel.loadMore()
                }
            }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.post_hint))
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painterResource(R.drawable.baseline_arrow_back_24),
                            contentDescription = stringResource(R.string.back_hint)
                        )
                    }
                })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onToPostFeed(null, zone) },
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
                .padding(horizontal = 15.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (uiState) {
                is FeedUiState.Loading -> LoadingCard()
                is FeedUiState.Error -> LoadErrorUiLayout(
                    onClick = { viewmodel.fetchData(zone) }
                )

                is FeedUiState.Success -> {
                    FeedUiLayout(
                        feed = (uiState as FeedUiState.Success).feedData.feed,
                        onClick = { onToFeedDetail(it, zone) },
                        listState = listState,
                        isLoadingMore = (uiState as FeedUiState.Success).isLoadingMore,
                        hasMore = (uiState as FeedUiState.Success).hasMore
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedUiLayout(
    feed: List<Feed>, listState: LazyListState, isLoadingMore: Boolean,
    hasMore: Boolean, onClick: (id: Int) -> Unit
) {
    LazyColumn(state = listState) {
        items(
            items = feed,
            key = { it.id }
        ) { feed ->
            FeedCard(
                feed,
                onClick = { onClick(feed.id) }
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
