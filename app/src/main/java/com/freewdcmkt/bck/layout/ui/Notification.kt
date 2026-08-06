package com.freewdcmkt.bck.layout.ui

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.components.NotificationCard
import com.freewdcmkt.bck.data.screen.NotificationData
import com.freewdcmkt.bck.viewmodel.NotificationUiStates
import com.freewdcmkt.bck.viewmodel.NotificationViewmodel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Notification(
    viewmodel: NotificationViewmodel = viewModel(),
    onToFeedDetail: (Int) -> Unit
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiStates by viewmodel.uiStates.collectAsState()
    val yesHint = stringResource(R.string.yes_hint)
    LaunchedEffect(Unit) { viewmodel.getNotification() }
    LaunchedEffect(uiStates) {
        if (uiStates is NotificationUiStates.LoadError) {
            scope.launch {
                val result = snackBarHostState.showSnackbar(
                    message = (uiStates as NotificationUiStates.LoadError).msg,
                    actionLabel = yesHint,
                    duration = SnackbarDuration.Short
                )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        viewmodel.getNotification()
                    }

                    else -> {}
                }
            }
        }
    }
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.notification_hint)) }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            when (uiStates) {
                is NotificationUiStates.Loading -> LoadingCard()
                is NotificationUiStates.Finish -> {
                    val data =  (uiStates as NotificationUiStates.Finish).notificationData
                    Log.d("NOTIFICATION UI",data.toString())
                    NotificationUi(
                       data,
                        onToFeedDetail = { type, feedId ->
                            if (type == 1 || type == 2) onToFeedDetail(feedId)
                        })
                }

                is NotificationUiStates.LoadError -> {

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
    LazyColumn(modifier = Modifier.padding(horizontal = 15.dp)) {
        if (notificationData.isNotEmpty()) {
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
        } else {
            item { Text("NO NOTIFICATION") }

        }
    }
}