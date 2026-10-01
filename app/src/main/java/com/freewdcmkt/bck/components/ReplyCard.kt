package com.freewdcmkt.bck.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.api.userAvatarUrl
import com.freewdcmkt.bck.components.freewd.ContentText
import com.freewdcmkt.bck.components.freewd.DateText
import com.freewdcmkt.bck.components.freewd.SmallUserIcon
import com.freewdcmkt.bck.components.freewd.UsernameText
import com.freewdcmkt.bck.data.screen.FeedReplyData


@Composable
fun ReplyCard(
    modifier: Modifier = Modifier,
    replyData: FeedReplyData,
    isDeleting: Boolean,
    onReplyUser: (String, String) -> Unit,
    onLongClick: () -> Unit
) {
    // 缩放
    val scale by animateFloatAsState(
        targetValue = if (isDeleting) 0.6f else 1f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "scale"
    )
    // 透明度
    val alpha by animateFloatAsState(
        targetValue = if (isDeleting) 0f else 1f,
        animationSpec = tween(durationMillis = 300),
        label = "alpha"
    )
    // 旋转（轻微歪斜，像被打飞）
    val rotation by animateFloatAsState(
        targetValue = if (isDeleting) -12f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "rotation"
    )
    // 向下坠落
    val offsetY by animateFloatAsState(
        targetValue = if (isDeleting) 40f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "offsetY"
    )
    // 水平方向轻微晃动（模拟碎裂方向）
    val offsetX by animateFloatAsState(
        targetValue = if (isDeleting) -20f else 0f,
        animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing),
        label = "offsetX"
    )

    Card(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
                rotationZ = rotation
                translationX = offsetX
                translationY = offsetY
                // 让缩放围绕卡片中心
                transformOrigin = TransformOrigin(0.5f, 0.5f)
            }
            .combinedClickable(
                onClick = { if (!isDeleting) onReplyUser(replyData.qq, replyData.username) },
                onLongClick = { if (!isDeleting) onLongClick() }
            ),
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
                DateText(replyData.date)
                ContentText(replyData.msg)
            }
        }
    }
}