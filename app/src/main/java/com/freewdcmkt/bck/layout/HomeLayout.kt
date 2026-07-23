package com.freewdcmkt.bck.layout

import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.freewdcmkt.bck.data.HomeData
import com.freewdcmkt.bck.viewmodel.HomeUiState
import com.freewdcmkt.bck.viewmodel.HomeViewmodel
import kotlinx.serialization.Serializable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeLayout(
    viewmodel: HomeViewmodel = viewModel(),
    onToFeed: (zone: Int) -> Unit,
    onToBrowser: (link: String) -> Unit
) {
    val username by viewmodel.username.collectAsState()
    val qq by viewmodel.userAccount.collectAsState()
    val uid by viewmodel.uid.collectAsState()
    val homeData by viewmodel.homeData.collectAsState()
    val homeUiState by viewmodel.homeUiState.collectAsState()

    val navController = rememberNavController()

    LaunchedEffect(Unit) { viewmodel.fetchData() }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.home_hint)) }) },
        bottomBar = { NavigationBar() { NavBar(navController) } }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavData.Home.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None },
                popExitTransition = { ExitTransition.None },
                popEnterTransition = { EnterTransition.None }
            ) {
                composable(NavData.Home.route) {
                    UiLayout(
                        qq,
                        username,
                        uid,
                        homeData,
                        onToFeed = onToFeed,
                        onToBrowser = onToBrowser,
                        uiState = homeUiState
                    )
                }
                composable(NavData.Me.route) { Text("TODO :)") }
            }
        }
    }
}

@Composable
private fun UiLayout(
    qq: String,
    username: String,
    uid: String,
    homeData: HomeData,
    uiState: HomeUiState,
    onToFeed: (Int) -> Unit,
    onToBrowser: (String) -> Unit,
    viewmodel: HomeViewmodel = viewModel()
) {
    val context = LocalContext.current
    PullToRefreshBox(
        isRefreshing = uiState is HomeUiState.Loading,
        onRefresh = { viewmodel.fetchData(forceRefresh = true) },
    ) {
        LazyColumn {
            item {
                HomeTopZone(
                    qq = qq,
                    username = username,
                    uid = uid,
                    homeData.notification.imageUrl
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
fun NavBar(navController: NavController) {
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
                alwaysShowLabel = true
            )
        }
    }
}

@Serializable
sealed class NavData(val route: String, @StringRes val label: Int, @DrawableRes val icon: Int) {
    object Home : NavData("Home", R.string.home_hint, R.drawable.home)
    object Me : NavData("Me", R.string.me_hint, R.drawable.personal_center)
}
