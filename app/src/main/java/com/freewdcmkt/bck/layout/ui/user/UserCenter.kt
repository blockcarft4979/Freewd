package com.freewdcmkt.bck.layout.ui.user

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.freewdcmkt.bck.R

@Composable
fun UserCenter(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(
            { Text(stringResource(R.string.user_center_hint)) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painterResource(R.drawable.baseline_arrow_back_24),
                        contentDescription = stringResource(R.string.back_hint)
                    )
                }
            })
    }) { innerPadding -> Column(modifier = Modifier.padding(innerPadding)) {
        Text("TODO:)") } }
}