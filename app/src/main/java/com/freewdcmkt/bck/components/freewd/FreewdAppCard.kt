package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl

@Composable
fun NotificationCard(
    id: Int,
    qq: String,
    username: String,
    date: String,
    type: Int,
    msg: String? = null,
    onClick: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .clickable(onClick = { onClick() })
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
            ) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = userAvatarUrl(qq),
                    ),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    UsernameText(username)
                    DateText(date)
                }

                when (type) {
                    1 -> {
                        ContentText(stringResource(R.string.like_feed_hint, username))
                    }
                    2 if msg != null -> {
                        ContentText(
                            stringResource(
                                R.string.reply_feed_hint,
                                msg
                            )
                        )
                    }
                    else -> {
                        ContentText(stringResource(R.string.unsupported_notification_hint))
                    }
                }
            }
        }
    }

}
@Composable
@Preview
private fun Show(){
    NotificationCard(
        1, "", "HDUHDU", "2026-08-13 00:12:10",
        type = 1,
        msg = "nfngnfiodg",
    ) { }
}