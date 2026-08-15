package com.freewdcmkt.bck.layout.nav

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
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
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.HomeTopZone
import com.freewdcmkt.bck.components.HomeZoneItemCard
import com.freewdcmkt.bck.components.NotificationIcon
import com.freewdcmkt.bck.components.freewd.FreewdDialog
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.layout.ui.user.Me
import com.freewdcmkt.bck.util.TokenManager
import com.freewdcmkt.bck.util.UserInfoManager
import com.freewdcmkt.bck.viewmodel.HomeUiState
import com.freewdcmkt.bck.viewmodel.HomeViewmodel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayout(
    viewmodel: HomeViewmodel = viewModel(),
    onToFeed: (zone: Int) -> Unit,
    onToBrowser: (link: String) -> Unit,
    onToNotification: () -> Unit,
    onToUserCenter: () -> Unit
) {
    val username by viewmodel.username.collectAsState()
    val qq by viewmodel.userAccount.collectAsState()
    val uid by viewmodel.uid.collectAsState()
    val notificationId by viewmodel.notificationId.collectAsState(null)
    
    val userData by viewmodel.verifyTokenData.collectAsState()
    val homeUiState by viewmodel.homeUiState.collectAsState()

    val retryHint = stringResource(R.string.retry_hint)
    val navController = rememberNavController()

    val snackBarHostState = remember() { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val isShowNotification = rememberSaveable() { mutableStateOf(false) }

    val noNetWorkHint = stringResource(R.string.no_internet_hint)

    LaunchedEffect(Unit) { if (homeUiState is HomeUiState.Loading) viewmodel.fetchData(true) }
    LaunchedEffect(TokenManager.getToken()) { viewmodel.verifyToken() }

    LaunchedEffect(homeUiState) {
        if (homeUiState is HomeUiState.Error) {
            if ((homeUiState as HomeUiState.Error).isNoNetWork) {
                scope.launch {
                    val result = snackBarHostState.showSnackbar(
                        message = noNetWorkHint,
                        actionLabel = retryHint,
                        duration = SnackbarDuration.Long
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            viewmodel.fetchData(true)
                        }

                        else -> {}
                    }
                }
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.home_hint))
                },
                actions = { IconButton(onClick = onToNotification) { NotificationIcon(userData.unreadCount) } })
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
                    when (homeUiState) {
                        is HomeUiState.Loading -> UiLayout(
                            qq,
                            username,
                            uid,
                            HomeData(null, emptyList()),
                            onToFeed = onToFeed,
                            onToBrowser = onToBrowser,
                            uiState = homeUiState,
                            onRefresh = { viewmodel.fetchData(true) }
                        )

                        is HomeUiState.Finish -> {
                            val homeData = (homeUiState as HomeUiState.Finish).homeData
                            Log.d("HOME NAV HOST",notificationId.toString()+homeData.notification.toString())
                            UiLayout(
                                qq,
                                username,
                                uid,
                                homeData = homeData,
                                onToFeed = onToFeed,
                                onToBrowser = onToBrowser,
                                uiState = homeUiState,
                                onRefresh = { viewmodel.fetchData(true) }
                            )
                            if (homeData.notification?.id != notificationId) {
                                isShowNotification.value = true
                                val notificationData = homeData.notification
                                if (isShowNotification.value) {
                                    FreewdDialog(
                                        onDismiss = { isShowNotification.value = false },
                                        onConfirm = {
                                            scope.launch {
                                                UserInfoManager.saveNotificationId(
                                                    notificationData?.id ?: 0
                                                )
                                            }
                                        },
                                        title = if (notificationData?.title != null) notificationData.title else "",
                                        msg = if (notificationData?.msg != null) notificationData.msg else "",
                                        hintMsg1 = stringResource(R.string.yes_hint),
                                        hintMsg2 = stringResource(R.string.cancel_hint)
                                    )
                                }
                            }
                        }

                        else -> {}
                    }

                }
                composable(NavData.Me.route) {
                    Me(
                        onToUserCenter = onToUserCenter
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UiLayout(
    qq: String,
    username: String,
    uid: String,
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
                HomeTopZone(
                    qq = qq,
                    username = username,
                    uid = uid,
                    homeData.notification?.imageUrl
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
