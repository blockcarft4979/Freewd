package com.freewdcmkt.bck.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.freewdcmkt.bck.util.formatTime

@Composable
fun TitleText(text: String) {
    Text(
        text = text,
        style = TextStyle(
            fontSize = 15.sp,
            lineHeight = 20.sp,
            //platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun ContentText(text: String) {
    Text(
        fontSize = 14.sp,
        text = text,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
fun UsernameText(text: String) {
    Text(
        fontWeight = FontWeight.SemiBold,
        text = text,
        style = TextStyle(
            fontSize = 14.sp,
            //lineHeight = 20.sp,
            // platformStyle = PlatformTextStyle(includeFontPadding = false)
        ),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun DateText(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(end = 8.dp)
                .size(3.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
        )
        Text(
            text = formatTime(text),
            style = TextStyle(
                fontSize = 10.sp,
                lineHeight = 16.sp,
                platformStyle = PlatformTextStyle(includeFontPadding = false)
            ),
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
@Preview
private fun Show() {
    DateText("2026-08-01 00:27:00")
}