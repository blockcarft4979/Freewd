package com.freewdcmkt.bck.layout.ui.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil3.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun PreviewImgUi(url: String) {
    val zoomState = rememberZoomState()
    Image(
        modifier = Modifier.fillMaxSize().zoomable(zoomState),
        painter = rememberAsyncImagePainter(url),
        contentDescription = stringResource(R.string.preview_image)
    )
}