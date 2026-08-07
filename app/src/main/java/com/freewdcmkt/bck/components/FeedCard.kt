package com.freewdcmkt.bck.components

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.ContentText
import com.freewdcmkt.bck.components.freewd.DateText
import com.freewdcmkt.bck.components.freewd.TitleText
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.data.screen.Feed

@Composable
fun FeedCard(feed: Feed, onClick: (id: Int) -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .clickable(onClick = { onClick(feed.id) })
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
                        model = userAvatarUrl(feed.qq),
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
                    UsernameText(feed.username)
                    DateText(feed.date)
                }

                if (feed.title != null) TitleText(feed.title)
                if (feed.msg != null) ContentText(feed.msg)
            }
        }
    }

}

@Composable
@Preview(showBackground = false)
fun ShowCard() {
    val feed = Feed(
        "WO SHI TITLE",
        "我是内容，你好世界\nI'm content hello world",
        100,
        "IM ", "",
        "2025-08-13"
    )
    FeedCard(feed) { }
}
