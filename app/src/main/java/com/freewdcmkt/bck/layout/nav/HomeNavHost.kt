package com.freewdcmkt.bck.layout.nav

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.HomeZoneItemCard
import com.freewdcmkt.bck.components.NotificationIcon
import com.freewdcmkt.bck.components.freewd.FreewdModalBottomSheet
import com.freewdcmkt.bck.components.freewd.UserCard
import com.freewdcmkt.bck.data.common.UserInfoData
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.layout.ui.user.Me
import com.freewdcmkt.bck.viewmodel.nav.HomeUiState
import com.freewdcmkt.bck.viewmodel.nav.HomeViewmodel
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayout(
    viewmodel: HomeViewmodel = viewModel(),
    onToFeed: (zone: Int) -> Unit,
    onToBrowser: (link: String) -> Unit,
    onToNotification: () -> Unit
) {
    val username by UserInfoData.username.collectAsState()
    val qq by UserInfoData.account.collectAsState()
    val uid by UserInfoData.uid.collectAsState()
    val unreadCount by UserInfoData.unreadNotificationCount.collectAsState()
    val isShowNotification by viewmodel.isShowNotification.collectAsState()
    val isShowNoNetwork by viewmodel.isShowNoNetwork.collectAsState()
    val homeUiState by viewmodel.homeUiState.collectAsState()
    val homeData by viewmodel.homeData.collectAsState()

    val retryHint = stringResource(R.string.retry_hint)
    val navController = rememberNavController()

    val snackBarHostState = remember() { SnackbarHostState() }

    val unknownError = stringResource(R.string.unknown_error)
    LaunchedEffect(isShowNoNetwork) {
        if (isShowNoNetwork) {
            val result = snackBarHostState.showSnackbar(
                message = unknownError,
                actionLabel = retryHint,
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    viewmodel.fetchData(true)
                }

                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    UserCard(userAvatarUrl(qq), username, uid)
                },
                actions = { IconButton(onClick = onToNotification) { NotificationIcon(unreadCount) } })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = { NavigationBar() { NavBar(navController) } }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            NavHost(
                // modifier = Modifier.padding(horizontal = 15.dp).fillMaxSize(),
                navController = navController,
                startDestination = NavData.Home.route,
                popEnterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { -it * 180 / 100 },
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(350, delayMillis = 50))
                },
                popExitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
                    )
                },
                enterTransition = {
                    slideInHorizontally(
                        initialOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(350, delayMillis = 50))
                },
                exitTransition = {
                    slideOutHorizontally(
                        targetOffsetX = { -it * 180 / 100 },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    )
                }
            ) {
                composable(NavData.Home.route) {
                    UiLayout(
                        homeData = homeData,
                        onToFeed = onToFeed,
                        onToBrowser = onToBrowser,
                        uiState = homeUiState,
                        onRefresh = { viewmodel.fetchData(true) })
                    if (homeUiState is HomeUiState.Finish && isShowNotification) {
                        val notificationData = homeData.notification
                        FreewdModalBottomSheet(
                            onDismiss = { viewmodel.dismissNotification(null) },
                            onConfirm = {
                                viewmodel.dismissNotification(
                                    notificationData?.id ?: 0
                                )
                            },
                            title = if (notificationData?.title != null) notificationData.title else "",
                            msg = if (notificationData?.msg != null) notificationData.msg else "",
                            stringResource(R.string.cancel_hint),
                            stringResource(R.string.yes_hint)
                        )
                    }

                }
                composable(NavData.Me.route) {
                    Me()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiLayout(
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
private fun NavBar(navController: NavController) {
    val items = listOf(NavData.Home, NavData.Me)
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = navBackStackEntry?.destination?.route ?: NavData.Home.route
    NavigationBar() {

        items.forEachIndexed { _, data ->

            NavigationBarItem(
                selected = currentRoute == data.route,
                onClick = {
                    // selectedIndex = index
                    navController.navigate(data.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
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

@Serializable
sealed class NavData(val route: String, val label: Int, val icon: Int) {
    object Home : NavData("Home", R.string.home_hint, R.drawable.home)
    object Me : NavData("Me", R.string.me_hint, R.drawable.personal_center)
}
