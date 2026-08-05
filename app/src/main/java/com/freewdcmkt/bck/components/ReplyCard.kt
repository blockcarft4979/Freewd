package com.freewdcmkt.bck.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.data.screen.FeedReplyData

@Composable
fun ReplyCard(replyData: FeedReplyData,onReplyUser:(String,String)-> Unit) {

        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .fillMaxWidth().clickable(onClick = { onReplyUser(replyData.qq,replyData.username) }),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        ) {
            Row(modifier = Modifier.padding(10.dp)) {
            SmallUserIcon(userAvatarUrl(replyData.qq))
            Column(
                modifier = Modifier.padding(start = 8.dp),
                verticalArrangement = Arrangement.Top
            ) {
                UsernameText(replyData.username)
                ContentText(replyData.msg)
                DateText(replyData.date)
            }
        }
    }
}
