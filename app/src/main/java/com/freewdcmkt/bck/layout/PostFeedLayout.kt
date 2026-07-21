package com.freewdcmkt.bck.layout

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostFeedLayout() {
    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.add_post_hint)) }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 15.dp)) {
            Text("TODO:)")
        }
    }
}
@Composable
fun PostFeedUiLayout(){

}