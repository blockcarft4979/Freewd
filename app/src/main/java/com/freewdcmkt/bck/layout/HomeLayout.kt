package com.freewdcmkt.bck.layout

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.HomeTopZone
import com.freewdcmkt.bck.components.HomeZoneItemCard
import com.freewdcmkt.bck.data.HomeData
import com.freewdcmkt.bck.viewmodel.HomeUiState
import com.freewdcmkt.bck.viewmodel.HomeViewmodel

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

    LaunchedEffect(Unit) { viewmodel.fetchData() }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.home_hint)) }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            PullToRefreshBox(
                isRefreshing = homeUiState is HomeUiState.Loading,
                onRefresh = { viewmodel.fetchData() },
            ) {
                UiLayout(
                    qq, username, uid, homeData,
                    onToFeed = onToFeed,
                    onToBrowser = onToBrowser
                )
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
    onToFeed: (Int) -> Unit,
    onToBrowser: (String) -> Unit
) {
    val context = LocalContext.current
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