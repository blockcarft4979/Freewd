package com.freewdcmkt.bck.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.data.screen.FeedReplyData

@Composable
fun ReplyCard(replyData: FeedReplyData) {
    Row(modifier = Modifier.padding(10.dp)) {
        SmallUserIcon(userAvatarUrl(replyData.qq))
        Column(modifier = Modifier.padding(start = 8.dp), verticalArrangement = Arrangement.Top) {
            UsernameText(replyData.username)
            ContentText(replyData.msg)
            DateText(replyData.date)
        }
        //Spacer(modifier = Modifier.height(1.dp).background(Color.Gray))
    }
}

@Preview(showBackground = true)
@Composable
private fun test() {
    val replyData =
        FeedReplyData("dhfuidhufihuis", "dufiiusdfhui", "dhfuiarhfuighsu", "fiodhfhu9sh")
    ReplyCard(replyData)
}