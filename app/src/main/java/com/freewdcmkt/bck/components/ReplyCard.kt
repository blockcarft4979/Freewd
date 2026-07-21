package com.freewdcmkt.bck.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.data.screen.FeedReplyData

@Composable
fun ReplyCard(replyData: FeedReplyData) {
    Row() {
        UserIcon(userAvatarUrl(replyData.qq))
        Column(modifier = Modifier.padding(start = 8.dp)) {
            UsernameText(replyData.username)

            ContentText(replyData.msg)
        }
    }
}