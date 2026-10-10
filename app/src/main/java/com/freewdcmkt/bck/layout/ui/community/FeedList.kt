package com.freewdcmkt.bck.layout.ui.community

import android.os.Build
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
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.FeedCard
import com.freewdcmkt.bck.components.freewd.FreewdFooter
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.components.ui.ProgressiveBlurScrim
import com.freewdcmkt.bck.components.ui.feedListTopPadding
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
    isRefresh: Boolean,
    topAppBarState: TopAppBarState,
    density: Density,
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

    // 渐进模糊底衬：Modifier.blur 只模糊自身内容，所以要把列表按同一滚动位置再画一份；
    // 而「半径随 y 递减」只能靠多层叠加，于是每层各需要一个独立的滚动状态。
    // 关键：必须**逐帧**镜像主列表的位置。之前用 80ms 采样，滚动时副本落后最多 80ms，
    // 糊的那份和真实卡片错位叠在一起，就是「顶栏和卡片重叠」的来源。
    val statusBarBlur = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
    val blurListStates = List(BlurLayerCount) { rememberLazyListState() }
    // 状态栏高度在 composition 阶段取好；heightOffset 交给内部在布局阶段读，
    // 避免滚动时逐帧重组整个 FeedListHost
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentPadding = remember(topAppBarState, density, statusBarHeight) {
        FeedListContentPadding(statusBarHeight, topAppBarState, density)
    }

    if (statusBarBlur && blurListStates.isNotEmpty()) {
        LaunchedEffect(listState, feedList) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }.collect { (index, offset) ->
                blurListStates.forEach { mirror -> mirror.scrollToItem(index, offset) }
            }
        }
        // 回到顶部 / 数据整体刷新时，副本的 index 可能已经越界，这里复位一下
        LaunchedEffect(feedList.isEmpty()) {
            blurListStates.forEach { it.scrollToItem(0, 0) }
        }
    }

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
                        contentPadding = contentPadding,
                        onToPreviewImg = onToPreviewImg,
                        onLike = onLike,
                        overlay = {
                            if (statusBarBlur && blurListStates.isNotEmpty()) {
                                ProgressiveBlurScrim(
                                    topAppBarState = topAppBarState,
                                    density = density
                                ) { layerIndex ->
                                    // 同一份列表、同一个滚动位置，每层再画一份
                                    FeedListContent(
                                        postsData = feedList,
                                        listState = blurListStates[layerIndex],
                                        isLoadingMore = isLoadingMore,
                                        hasMore = hasMore,
                                        contentPadding = contentPadding,
                                        onClick = {},
                                        onToPreviewImg = {},
                                        onLike = {},
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
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
    contentPadding: PaddingValues,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit,
    // 画在列表之上、顶栏之下的东西（状态栏毛玻璃底衬）
    overlay: @Composable () -> Unit = {}
) {
    Box(modifier = Modifier.fillMaxSize()) {
        FeedListContent(
            postsData = postsData,
            listState = listState,
            isLoadingMore = isLoadingMore,
            hasMore = hasMore,
            contentPadding = contentPadding,
            onClick = onClick,
            onToPreviewImg = onToPreviewImg,
            onLike = onLike,
            modifier = Modifier.fillMaxSize()
        )
        overlay()
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
    contentPadding: PaddingValues,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (String) -> Unit,
    onLike: (id: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        contentPadding = contentPadding,
        modifier = modifier
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

/**
 * 列表上内边距 = 顶栏底边当前的 y 坐标，**在布局阶段**才读取折叠状态。
 *
 * 直接在 composable 里读 [TopAppBarState.heightOffset] 会让它在 composition 阶段
 * 被订阅，滚动时整棵 FeedListHost 每帧重组一次；`LazyColumn` 是在测量阶段
 * 调用 `calculateTopPadding()` 的，所以把读取放进这里，状态变化只会让列表重新测量。
 */
@Immutable
private class FeedListContentPadding(
    private val statusBarHeight: Dp,
    private val topAppBarState: TopAppBarState,
    private val density: Density
) : PaddingValues {

    override fun calculateTopPadding(): Dp =
        feedListTopPadding(statusBarHeight, topAppBarState, density)

    override fun calculateBottomPadding(): Dp = 0.dp

    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp = 0.dp

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp = 0.dp
}

