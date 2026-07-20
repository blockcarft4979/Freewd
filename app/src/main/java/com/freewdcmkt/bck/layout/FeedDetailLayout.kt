package com.freewdcmkt.bck.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedDetailLayout(id: String) {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.feed_detail_hint)) }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            Row { }
            Text("TODO:)$id")
        }
    }
}