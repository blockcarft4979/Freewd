package com.freewdcmkt.bck.components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.SubcomposeAsyncImage
import com.freewdcmkt.bck.R
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreviewImgUi(url: String, onDismiss: () -> Unit) {
    val zoomState = rememberZoomState()
    // 获取当前缩放比例，用于判断是否允许点击关闭
    val scale = zoomState.scale
    if (url.isEmpty()) return
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize() // 全屏
                .background(Color.Black) // 黑底
                .pointerInput(Unit) {
                    // 点击图片本身关闭，但如果放大了就不关（体验更好）
                    detectTapGestures(
                        onTap = {
                            if (scale <= 1f) {
                                onDismiss()
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = url,
                contentDescription = stringResource(R.string.image_hint),
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize() // 填满整个 Box
                    .zoomable(zoomState),
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        LoadingIndicator(
                            modifier = Modifier.size(48.dp)
                        )
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.load_error_hint),
                            color = Color.White
                        )
                    }
                }
            )
        }
    }
}