package com.freewdcmkt.bck.components.freewd

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun UserIcon(url: String) {
    Image(
        painter = rememberAsyncImagePainter(url), null, modifier = Modifier
            .size(48.dp)
            .padding(5.dp)
            .clip(CircleShape)
    )
}
@Composable
fun SmallUserIcon(url: String){
    Image(
        painter = rememberAsyncImagePainter(url), null, modifier = Modifier
            .size(40.dp)
            .padding(5.dp)
            .clip(CircleShape)
    )
}