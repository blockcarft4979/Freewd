package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.NotificationCard
import com.freewdcmkt.bck.components.ui.FreewdHint
import com.freewdcmkt.bck.components.ui.LoadingCard
import com.freewdcmkt.bck.data.screen.NotificationData
import com.freewdcmkt.bck.viewmodel.user.NotificationUiStates
import com.freewdcmkt.bck.viewmodel.user.NotificationViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Notification(
    viewmodel: NotificationViewmodel = viewModel(),
    onToFeedDetail: (Int) -> Unit,
    onBack: () -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val uiStates by viewmodel.uiStates.collectAsState()
    val expand = remember { mutableStateOf(false) }
    val noNetworkHint = stringResource(R.string.no_internet_hint)
    val yesHint = stringResource(R.string.yes_hint)
    LaunchedEffect(Unit) { viewmodel.getNotification() }

    LaunchedEffect(uiStates) {
        if (uiStates is NotificationUiStates.LoadError) {
            val error = (uiStates as NotificationUiStates.LoadError)
            if (error.isNoNetwork) {
                snackBarHostState.showSnackbar(noNetworkHint, yesHint)
            } else {
                (uiStates as NotificationUiStates.LoadError).msg?.let {
                    snackBarHostState.showSnackbar(
                        it
                    )
                }
            }

        }
    }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text(stringResource(R.string.notification_hint)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_24),
                        stringResource(R.string.back_hint)
                    )
                }
            },
            actions = {
                if (uiStates is NotificationUiStates.Finish && (uiStates as NotificationUiStates.Finish).notificationData.isNotEmpty()) IconButton(
                    onClick = { expand.value = true },
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_more_vert_24),
                        stringResource(R.string.more_hint)
                    )
                    DropdownMenu(
                        expand.value,
                        onDismissRequest = {
                            expand.value = false
                        }) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.clear_all_notifications_hint)) },
                            onClick = {
                                expand.value = false
                                viewmodel.clearNotifications(true)
                            })
                    }
                }
            })
    }, snackbarHost = { SnackbarHost(snackBarHostState) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (uiStates) {
                is NotificationUiStates.Loading -> LoadingCard()
                is NotificationUiStates.Finish -> {
                    val data = (uiStates as NotificationUiStates.Finish).notificationData
                    NotificationUi(
                        data,
                        onToFeedDetail = { type, feedId ->
                            if (type == 1 || type == 2) onToFeedDetail(feedId)
                        })
                }

                is NotificationUiStates.LoadError -> {
                    FreewdHint(
                        icon = R.drawable.baseline_error_24,
                        hint = stringResource(R.string.load_error_hint)
                    )
                }
            }
        }

    }
}

@Composable
private fun NotificationUi(
    notificationData: List<NotificationData>,
    onToFeedDetail: (Int, Int) -> Unit
) {
    if (notificationData.isNotEmpty()) {
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 15.dp)
        ) {

            items(
                items = notificationData,
                key = { it.id }
            ) { data ->
                NotificationCard(
                    id = data.id,
                    qq = data.fromQq,
                    username = data.fromUsername,
                    date = data.createdAt,
                    type = data.type,
                    msg = data.preview,
                    onClick = { onToFeedDetail(data.type, data.feedId) }
                )
            }
        }
    } else {
        FreewdHint(
            icon = R.drawable.baseline_notifications_none_24,
            hint = stringResource(R.string.no_notifications_hint)
        )
    }
}