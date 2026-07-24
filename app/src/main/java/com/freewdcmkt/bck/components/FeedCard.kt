package com.freewdcmkt.bck.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.freewdcmkt.bck.data.screen.Feed

@Composable
fun FeedCard(feed: Feed, onClick: (id: Int) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = { onClick(feed.id) })
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                    //.padding(horizontal = 4.dp, vertical = 2.dp), // 整体外间距，与卡片内边距协调
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp) // 头像和文字之间的间距
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            model = userAvatarUrl(feed.qq),
                            // 可选的加载占位图、错误图（需要 Coil 的扩展）
                            // placeholder = painterResource(R.drawable.ic_avatar_placeholder),
                            // error = painterResource(R.drawable.ic_avatar_error)
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

                // 文字区域
                Column(
                    modifier = Modifier.weight(1f), // 让文字占据剩余空间
                    verticalArrangement = Arrangement.Center
                ) {
                    UsernameText(feed.username)
                    DateText(feed.date)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (feed.title != null) TitleText(feed.title)
            if (feed.msg != null) ContentText(feed.msg)
        }
    }
}

@Composable
@Preview
fun ShowCard() {
    val feed = Feed(
        "WO SHI TITLE",
        "我是内容，你好世界\nI'm content hello world",
        100,
        "IM USERNAME WELCOME TO MY WORLD NICE TO MEET YOU :)",
        "",
        "2025-08-13"
    )
    FeedCard(feed) { }
}
