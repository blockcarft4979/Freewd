package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.data.screen.FeedDetailData
import com.freewdcmkt.bck.data.screen.PostsData

@Composable
fun FeedCard(
    feed: PostsData,
    onClick: (id: Int) -> Unit,
    onToPreviewImg: (url: String) -> Unit,
    onLike: (id: Int) -> Unit,
    onClickUserIcon: () -> Unit = {}
) {


    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = { onClick(feed.id) })
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (feed.qq == "0") FreewdIcon() else UserIcon(
                userAvatarUrl(feed.qq),
                onClick = onClickUserIcon
            )

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
                if (feed.img != null) {
                    Spacer(modifier = Modifier.heightIn(4.dp))
                    ImageCard(
                        feed.img,
                        onClick = { onToPreviewImg(feed.img) }
                    )
                }
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
fun FeedDetailCard(
    feed: FeedDetailData,
    onToPreviewImg: (url: String) -> Unit,
    onLike: (id: Int) -> Unit,
    onClickUserIcon: () -> Unit = {}
) {


    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 15.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (feed.qq == "0") FreewdIcon() else UserIcon(
                userAvatarUrl(feed.qq),
                onClick = onClickUserIcon
            )
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
                SelectionContainer {
                    if (feed.title?.isNotBlank() == true) TitleText(feed.title)
                    if (feed.msg != null) ContentText(feed.msg)
                }

                if (feed.img != null) {
                    Spacer(modifier = Modifier.heightIn(4.dp))
                    ImageCard(
                        feed.img,
                        onClick = { onToPreviewImg(feed.img) }
                    )
                }
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


