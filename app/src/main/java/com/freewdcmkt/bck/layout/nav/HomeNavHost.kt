package com.freewdcmkt.bck.layout.nav

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.NotificationIcon
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.HomeZoneItemCard
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.layout.ui.user.Me
import com.freewdcmkt.bck.viewmodel.nav.HomeUiState
import com.freewdcmkt.bck.viewmodel.nav.HomeViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeNavHost(
    viewmodel: HomeViewmodel = viewModel(),
    onToFeed: (zone: Int) -> Unit,
    onToBrowser: (link: String) -> Unit,
    onToNotification: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

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
    val unknownError = stringResource(R.string.unknown_error)

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { UserCard(userAvatarUrl(qq), username, uid) },
                actions = {
                    IconButton(onClick = onToNotification) {
                        NotificationIcon(unreadCount)
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            NavigationBar {
                NavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            when (selectedTab) {
                0 -> {
                    HomeLayout(
                        homeData = homeData,
                        onToFeed = onToFeed,
                        onToBrowser = onToBrowser,
                        uiState = homeUiState,
                        onRefresh = { viewmodel.fetchData(true) }
                    )
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

                1 -> Me()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeLayout(
    homeData: HomeData,
    uiState: HomeUiState,
    onToFeed: (Int) -> Unit,
    onToBrowser: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    PullToRefreshBox(
        isRefreshing = uiState is HomeUiState.Loading,
        onRefresh = onRefresh,
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                if (homeData.notification?.imageUrl != null) Image(
                    painter = rememberAsyncImagePainter(homeData.notification.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .aspectRatio(16f / 9f),
                    contentScale = ContentScale.Crop
                )
            }
            items(
                items = homeData.zone,
                key = { "${it.description}_${it.name}_${it.icon}" }) { zone ->

                HomeZoneItemCard(zone, onClick = {
                    if (zone.msg != null) Toast.makeText(
                        context,
                        zone.msg,
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    if (zone.zone != null) onToFeed(zone.zone)
                    if (zone.link != null) onToBrowser(zone.link)
                })
            }

        }
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
