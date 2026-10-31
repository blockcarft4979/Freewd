package com.freewdcmkt.bck.layout.nav

import android.app.Application
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.NotificationIcon
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.components.ui.PreviewImgUi
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.layout.ui.community.FeedListHost
import com.freewdcmkt.bck.layout.ui.user.Me
import com.freewdcmkt.bck.viewmodel.community.FeedListViewmodel
import com.freewdcmkt.bck.viewmodel.community.FeedUiState
import com.freewdcmkt.bck.viewmodel.nav.HomeUiState
import com.freewdcmkt.bck.viewmodel.nav.HomeViewmodel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavHost(
    feedListViewmodel: FeedListViewmodel = viewModel(),
    onToFeedDetail: (id: Int) -> Unit,
    onToPostFeed: () -> Unit,
    onToNotification: () -> Unit,
    onToSearch: () -> Unit,
    onToAboutApp: () -> Unit,
    onToUserCenter: () -> Unit,
    onToSettings: () -> Unit,
    onToUserProfile:(qq: String)-> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val viewmodel: HomeViewmodel = viewModel(
        factory = viewModelFactory {
            initializer { HomeViewmodel(context.applicationContext as Application) }
        }
    )
    val username by UserInfoData.username.collectAsState()
    val qq by UserInfoData.account.collectAsState()
    val uid by UserInfoData.uid.collectAsState()
    val unreadCount by UserInfoData.unreadNotificationCount.collectAsState()
    val isShowNotification by viewmodel.isShowNotification.collectAsState()
    val isShowNoNetwork by viewmodel.isShowNoNetwork.collectAsState()
    val homeUiState by viewmodel.homeUiState.collectAsState()
    val homeData by viewmodel.homeData.collectAsState()

    val retryHint = stringResource(R.string.retry_hint)
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(state = topAppBarState)
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val unknownError = stringResource(R.string.unknown_error)

    LaunchedEffect(isShowNoNetwork) {
        if (isShowNoNetwork) {
            val result = snackBarHostState.showSnackbar(
                message = unknownError,
                actionLabel = retryHint,
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                viewmodel.fetchData(true)
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewmodel.verifyToken()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet {
                Me(
                    onToAboutApp = onToAboutApp,
                    onToUserCenter = onToUserCenter, onToSettings = onToSettings
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        UserCard(
                            userAvatarUrl(qq),
                            username,
                            uid,
                            onClick = { scope.launch { drawerState.open() } })
                    },
                    actions = {
                        IconButton(onClick = onToSearch) {
                            Icon(
                                painter = painterResource(R.drawable.baseline_search_24),
                                contentDescription = stringResource(R.string.search_hint)
                            )
                        }
                        IconButton(onClick = onToNotification) {
                            NotificationIcon(unreadCount)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = Color.Transparent
                    )
                )
            },
            snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        ) { innerPadding ->

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
            ) {
                HomeUI(
                    feedListViewmodel = feedListViewmodel,
                    onToPostFeed = onToPostFeed,
                    onToFeedDetail = onToFeedDetail,
                    onLike = { id -> feedListViewmodel.toggleLike(id) },
                    onToUserProfile = onToUserProfile
                )
            }
        }
    }

    if (homeUiState is HomeUiState.Finish && isShowNotification) {
        val notificationData = homeData.notification
        FreewdModalBottomSheet(
            onDismiss = { viewmodel.dismissNotification(null) },
            onConfirm = {
                viewmodel.dismissNotification(notificationData?.id ?: 0)
            },
            title = notificationData?.title ?: "",
            msg = notificationData?.msg ?: "",
            stringResource(R.string.cancel_hint),
            stringResource(R.string.yes_hint)
        )
    }

}

@Composable
private fun HomeUI(
    modifier: Modifier = Modifier,
    onToFeedDetail: (id: Int) -> Unit,
    onToPostFeed: () -> Unit,
    onLike: (id: Int) -> Unit,
    onToUserProfile: (qq: String) -> Unit,
    homeViewmodel: HomeViewmodel = viewModel(),
    feedListViewmodel: FeedListViewmodel
) {
    val refreshViewmodel: RefreshStateViewModel = viewModel()
    val isRefresh = refreshViewmodel.feedRefresh
    val homeUiState by homeViewmodel.homeUiState.collectAsState()
    val feedUiState by feedListViewmodel.feedUiState.collectAsState()

    var imgUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(isRefresh) {
        if (isRefresh) {
            feedListViewmodel.fetchData(true)
            refreshViewmodel.feedRefresh = false
        }
    }

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = homeUiState is HomeUiState.Loading,
        onRefresh = {
            homeViewmodel.fetchData(true)
            feedListViewmodel.fetchData(true)
        }) {
        FeedListHost(
            isRefresh = feedUiState is FeedUiState.Loading,
            onToFeedDetail = onToFeedDetail,
            onToPostFeed = onToPostFeed,
            onToPreviewImg = { url -> imgUrl = url },
            onLike = onLike,
            onToUserProfile = onToUserProfile
        )
        PreviewImgUi(url = imgUrl ?: "", onDismiss = { imgUrl = null })
    }
}

@Composable
private fun NavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = NavTab.entries

    NavigationBar {
        items.forEachIndexed { index, data ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = {
                    Icon(
                        painter = painterResource(data.icon),
                        contentDescription = stringResource(data.label),
                        modifier = Modifier.size(32.dp)
                    )
                },
                label = { Text(stringResource(data.label)) },
                alwaysShowLabel = false
            )
        }
    }
}

enum class NavTab(val label: Int, val icon: Int) {
    Home(R.string.home_hint, R.drawable.home),
    Me(R.string.me_hint, R.drawable.personal_center)
}
