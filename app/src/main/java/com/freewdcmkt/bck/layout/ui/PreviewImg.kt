package com.freewdcmkt.bck.layout.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import coil.compose.rememberAsyncImagePainter
import com.freewdcmkt.bck.R

@Composable
fun PreviewImgUi(url: String) {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = rememberAsyncImagePainter(url),
        contentDescription = stringResource(R.string.preview_image)
    )
}