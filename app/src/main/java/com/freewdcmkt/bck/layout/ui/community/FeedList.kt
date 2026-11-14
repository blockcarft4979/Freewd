package com.freewdcmkt.bck.layout.ui.community

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.components.freewd.FeedCard
import com.freewdcmkt.bck.components.freewd.FreewdFooter
import com.freewdcmkt.bck.components.freewd.LoadingCard
import com.freewdcmkt.bck.data.screen.PostsData
import com.freewdcmkt.bck.viewmodel.community.FeedListViewmodel
import com.freewdcmkt.bck.viewmodel.community.FeedUiState
import kotlinx.coroutines.flow.distinctUntilChanged

/** 渐进模糊的层数，需要和 [ProgressiveBlurScrim] 默认的 layers 数量一致 */
private const val BlurLayerCount = 3

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedListHost(
    viewmodel: FeedListViewmodel = viewModel(),
    listState: LazyListState,
    onToFeedDetail: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit,
    onToUserProfile: (uid: Int) -> Unit
) {
    val uiState by viewmodel.feedUiState.collectAsState()
    val feedListData by viewmodel.feedListData.collectAsState()
    val isLoadingMore by viewmodel.isLoadingMore.collectAsState()
    val hasMore by viewmodel.hasMore.collectAsState()

    val listState = listState

    val feedList = feedListData?.feed ?: emptyList()
    val fabVisible by remember { derivedStateOf { !listState.isScrollInProgress } }



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


    Column(
        modifier = Modifier
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
                    onLike = onLike,
                    onToUserProfile = onToUserProfile,
                )
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
    onToUserProfile: (uid: Int) -> Unit,
    onLike: (id: Int) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FeedListContent(
            postsData = postsData,
            listState = listState,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            onClick = onClick,
            onToPreviewImg = onToPreviewImg,
            onLike = onLike,
            onToUserProfile = onToUserProfile,
            modifier = Modifier.fillMaxSize()
        )
        //overlay()
    }
}

/**
 * 列表本体。抽出来是因为状态栏毛玻璃需要「同一份列表内容、同一个滚动位置」
 * 再布局一份出来当底衬（`Modifier.blur` 只能模糊自身内容，采样不到背后的兄弟节点，
 * 所以背景模糊只能靠再画一份）。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeedListContent(
    postsData: List<PostsData>,
    listState: LazyListState,
    isLoadingMore: Boolean,
    hasMore: Boolean,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit,
    onToUserProfile: (uid: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(
            items = postsData, key = { it.id }) { feed ->
            FeedCard(
                feed,
                onClick = onClick,
                onToPreviewImg = onToPreviewImg,
                onLike = onLike,
                onClickUserIcon = { onToUserProfile(feed.uid) }
            )
        }
        if (isLoadingMore) {
            item {
                LoadingCard(
                    modifier = Modifier
                        .padding(16.dp)
                        .height(64.dp)
                )
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

