package com.freewdcmkt.bck.layout.ui.community

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.freewdcmkt.bck.data.screen.PostsData
import com.freewdcmkt.bck.viewmodel.community.FeedListViewmodel
import com.freewdcmkt.bck.viewmodel.community.FeedUiState
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListHost(
    viewmodel: FeedListViewmodel = viewModel(),
    isRefresh: Boolean,
    onToFeedDetail: (id: Int) -> Unit,
    onToPostFeed: () -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit
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
                .fillMaxSize()
        ) {
            when {

                uiState is FeedUiState.Loading && feedList.isEmpty() -> LoadingCard()

                else -> {

                    FeedListUi(
                        postsData = feedList,
                        onClick = onToFeedDetail,
                        listState = listState,
                        isLoadingMore = isLoadingMore,
                        hasMore = hasMore,
                        onToPreviewImg = onToPreviewImg,
                        onLike = onLike
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedListUi(
    postsData: List<PostsData>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit
) {
  //  val stateBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    LazyColumn(
        state = listState,
//        contentPadding = PaddingValues(
//            top = stateBarHeight + 64.dp,   // 状态栏 + TopAppBar 高度
//            bottom = 88.dp                    // 给 FAB 留位置
//        )
    ) {
        items(
            items = postsData, key = { it.id }) { feed ->
            FeedCard(
                feed,
                onClick = onClick,
                onToPreviewImg = onToPreviewImg,
                onLike = onLike
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
        } else if (!hasMore && postsData.isNotEmpty()) {
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
