package com.freewdcmkt.bck.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.ContentText
import com.freewdcmkt.bck.components.freewd.DateText
import com.freewdcmkt.bck.components.freewd.FreewdIcon
import com.freewdcmkt.bck.components.freewd.IconTextButton
import com.freewdcmkt.bck.components.freewd.ImageCard
import com.freewdcmkt.bck.components.freewd.TitleText
import com.freewdcmkt.bck.components.freewd.UserIcon
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.data.screen.PostsData

@Composable
fun FeedCard(
    feed: PostsData,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (url: String) -> Unit,
    onLike: (id: Int) -> Unit
) {

    // var imgUrl by rememberSaveable() { mutableStateOf<String?>(null) }

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .clickable(onClick = { onClick(feed.id) })
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (feed.qq == "0") FreewdIcon() else UserIcon(userAvatarUrl(feed.qq))

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                UsernameText(feed.username)
                DateText(feed.date)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                }

                if (feed.title != null) TitleText(feed.title)
                if (feed.msg != null) ContentText(feed.msg)
                if (feed.img != null) ImageCard(
                    feed.img,
                    onClick = { onToPreviewImg(feed.img) }
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconTextButton(
                        icon = if (feed.isLiked) R.drawable.baseline_favorite_24 else R.drawable.baseline_favorite_border_24,
                        text = feed.likeCount.toString(),
                        onClick = { onLike(feed.id) }
                    )
                }
            }

        }
    }

}

@Composable
@Preview(showBackground = false)
fun ShowCard() {
    val feed = PostsData(
        "WO SHI TITLE",
        "我是内容，你好世界\n南梁写代码拯救世界！喵~~",
        100,
        "IM ", "",
        "2025-08-13",
        30000,
        true,
        "11"
    )
    FeedCard(
        feed,
        onClick = { }, onToPreviewImg = {}, onLike = {}
    )
}
