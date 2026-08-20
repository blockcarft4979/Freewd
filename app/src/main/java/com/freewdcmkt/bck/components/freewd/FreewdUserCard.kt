package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@Composable
fun UserCard(imageUrl: String, username: String, uid: String) {
    Column {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserIcon(imageUrl)
            Column() {
                UsernameText(username)
                UidText(stringResource(R.string.uid_hint,uid) )
            }
        }
    }
}

@Composable
@Preview
private fun Show() {
    UserCard("", "BUHSUIHSUI", "21115")
}