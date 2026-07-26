package com.freewdcmkt.bck.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.LoadErrorUiLayout
import com.freewdcmkt.bck.components.LoadingCard
import com.freewdcmkt.bck.viewmodel.PostFeedUiState
import com.freewdcmkt.bck.viewmodel.PostFeedViewmodel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedLayout(zone: Int, onUploaded: () -> Unit, viewmodel: PostFeedViewmodel = viewModel()) {
    val uiState by viewmodel.postFeedUiState.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.add_post_hint)) }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            when (uiState) {
                is PostFeedUiState.NoAction -> PostFeedUiLayout(onPostFeed = { title, message ->
                    viewmodel.postFeed(
                        zone = zone,
                        title = title,
                        message = message
                    )
                })

                is PostFeedUiState.Upload -> LoadingCard()
                is PostFeedUiState.Error -> LoadErrorUiLayout(
                    onClick = { viewmodel.postFeedUiState },
                    msg = (uiState as PostFeedUiState.Error).msg,
                    buttonMsg = stringResource(R.string.retry_hint),
                    icon = painterResource(R.drawable.baseline_refresh_24)
                )

                is PostFeedUiState.Success -> onUploaded()

            }
        }
    }
}

@Composable
fun PostFeedUiLayout(onPostFeed: (title: String?, message: String) -> Unit) {
    var title by rememberSaveable() { mutableStateOf("") }
    var message by rememberSaveable() { mutableStateOf("") }
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = title,
        onValueChange = { newTitle -> title = newTitle },
        label = { Text(stringResource(R.string.title_hint)) },
        maxLines = 1
    )
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = message,
        onValueChange = { newMessage -> message = newMessage },
        label = { Text(stringResource(R.string.content_hint)) })
    Button(modifier = Modifier.fillMaxWidth(), onClick = { onPostFeed(title, message) }) {
        Text(
            stringResource(R.string.post_hint)
        )
    }
}