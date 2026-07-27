package com.freewdcmkt.bck.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.data.screen.Zone

@Composable
fun HomeZoneItemCard(
    zone: Zone,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                true,
                onClick = { onClick() }
            ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        ),

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),  // 内边距更宽松
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)   // 自动控制间距
        ) {
            // 图标区域：圆形背景衬托
            Box(
                modifier = Modifier
                    .size(48.dp)                     // 图标容器变大
                    .clip(CircleShape)
                   .background(MaterialTheme.colorScheme.primaryContainer) // 背景色取主题容器色
            ) {
                Icon(
                    painter = rememberAsyncImagePainter(zone.icon),
                    contentDescription = zone.name,
                    modifier = Modifier
                        .size(32.dp)                 // 图标本身大小
                        .align(Alignment.Center),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            // 文字区域
            Column(
                modifier = Modifier.weight(1f),      // 占据剩余宽度
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = zone.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,   // 比 Bold 柔和一些
                    color = MaterialTheme.colorScheme.onSurface
                )
                zone.description?.let {
                    Text(
                        text = it,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal,  // 不用 Thin，阅读更舒适
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// 预览（需提供示例 Zone）
@Preview(showBackground = true)
@Composable
fun PreviewHomeZoneItemCard() {
    MaterialTheme {
        HomeZoneItemCard(
            zone = Zone(
                name = "休息区",
                description = "舒适沙发，适合小憩",
                icon = "https://example.com/icon.png"
            ),
            onClick = { }
        )
    }
}