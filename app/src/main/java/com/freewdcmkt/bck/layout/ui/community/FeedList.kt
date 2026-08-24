package com.freewdcmkt.bck.layout.ui.community

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
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
fun FeedLayout(
    viewmodel: FeedListViewmodel = viewModel(),
    isRefresh: Boolean,
    zone: Int,
    onToFeedDetail: (id: Int, zone: Int) -> Unit,
    onToPostFeed: (id: Int?, zone: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onBack: () -> Unit,
) {
    val uiState by viewmodel.feedUiState.collectAsState()
    val listState = viewmodel.listState
    val snackBarHostState = remember { SnackbarHostState() }
    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    LaunchedEffect(zone) { viewmodel.fetchData(zone) }
    LaunchedEffect(uiState) {
        if (uiState is FeedUiState.Error && (uiState as FeedUiState.Error).msg != null) (uiState as FeedUiState.Error).msg?.let {
            snackBarHostState.showSnackbar(
                it
            )
        }
    }
    LaunchedEffect(isRefresh) {
        if (isRefresh) {
            listState.scrollToItem(0)
            viewmodel.fetchData(zone, forceRefresh = true)
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
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            //TopAppBar()
            LargeTopAppBar(
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
                },
                scrollBehavior = scrollBehavior
            )
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
        }, snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (uiState) {
                is FeedUiState.Loading -> LoadingCard()
                is FeedUiState.Success -> {
                    FeedUiLayout(
                        feed = (uiState as FeedUiState.Success).feedData.feed,
                        onClick = { onToFeedDetail(it, zone) },
                        listState = listState,
                        isLoadingMore = (uiState as FeedUiState.Success).isLoadingMore,
                        hasMore = (uiState as FeedUiState.Success).hasMore,
                        onToPreviewImg = onToPreviewImg
                    )
                }

                is FeedUiState.Error -> {}
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedUiLayout(
    feed: List<Feed>, listState: LazyListState, isLoadingMore: Boolean,
    hasMore: Boolean, onClick: (id: Int) -> Unit, onToPreviewImg: (String) -> Unit
) {
    LazyColumn(state = listState) {
        items(
            items = feed,
            key = { it.id }
        ) { feed ->
            FeedCard(
                feed,
                onClick = { onClick(feed.id) },
                onToPreviewImg = onToPreviewImg
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
