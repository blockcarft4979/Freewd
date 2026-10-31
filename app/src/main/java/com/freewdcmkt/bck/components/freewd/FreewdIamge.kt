package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R

@Composable
fun UserIcon(url: String, size: Dp = 48.dp, onClick: () -> Unit = {}) {
    Image(
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(size)
            .padding(5.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    )
}

@Composable
fun FreewdIcon() {
    Icon(
        painter = painterResource(R.mipmap.ic_launcher_monochrome), null, modifier = Modifier
            .size(48.dp)
            .padding(5.dp)
            .clip(CircleShape)
    )
}

@Composable
fun SmallUserIcon(url: String, onClick: () -> Unit = {}) {
    Image(
        painter = rememberAsyncImagePainter(url),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(40.dp)
            .padding(5.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    )
}