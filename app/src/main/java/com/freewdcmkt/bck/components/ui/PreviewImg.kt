package com.freewdcmkt.bck.components.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.size.Precision
import com.freewdcmkt.bck.R
import kotlinx.coroutines.CancellationException
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PreviewImgUi(url: String, onDismiss: () -> Unit) {
    if (url.isEmpty()) return

    val context = LocalContext.current
    val zoomState = rememberZoomState()
    val scale = zoomState.scale

    // ============ 高清加载：按「窗口宽 × 最大缩放倍率」取图 ============
    val containerSize = LocalWindowInfo.current.containerSize
    val maxScale = 3f
    val targetWidth = remember(containerSize.width) {
        (containerSize.width * maxScale).toInt().coerceAtLeast(1)
    }

    val imageRequest = remember(url, targetWidth) {
        ImageRequest.Builder(context)
            .data(url)
            .size(targetWidth)
            .precision(Precision.EXACT)
            .allowHardware(false)
            .build()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false,
            // dismissOnBackPress 保持默认 true：
            // - 未放大：PredictiveBackHandler 接管，走自定义动画
            // - 已放大：PredictiveBackHandler 禁用，系统默认直接关闭
        )
    ) {
        // 以下都在 Dialog 自己的窗口里，PredictiveBackHandler 会绑定到 Dialog 的 dispatcher
        val view = LocalView.current
        var backProgress by remember { mutableFloatStateOf(0f) }

        // 1. 关闭平台窗口默认动画，避免和预测性返回动画打架
        LaunchedEffect(Unit) {
            (view.parent as? DialogWindowProvider)?.window?.let { window ->
                window.setWindowAnimations(0)
                window.setDimAmount(0f)
            }
        }

        // 2. 全屏沉浸式：隐藏系统栏，边缘滑动临时唤出
        DisposableEffect(Unit) {
            val window = (view.parent as? DialogWindowProvider)?.window
            val controller = window?.let {
                WindowCompat.getInsetsController(it, it.decorView)
            }
            controller?.let {
                it.hide(WindowInsetsCompat.Type.systemBars())
                it.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
            onDispose {
                controller?.show(WindowInsetsCompat.Type.systemBars())
            }
        }

        // 3. 预测性返回
        //    scale <= 1f：驱动退出动画（背景淡出 + 图片微缩）
        //    scale > 1f：禁用，让 zoomable 的平移手势优先；同时系统默认 dismissOnBackPress 兜底
        PredictiveBackHandler(enabled = scale <= 1f) { progress ->
            try {
                progress.collect { event ->
                    backProgress = event.progress
                }
                onDismiss()
            } catch (e: CancellationException) {
                // 手势取消（用户滑回），恢复原位
                backProgress = 0f
            }
        }

        // ============ UI ============
        val bgAlpha = (1f - backProgress * 1.3f).coerceIn(0f, 1f)
        val exitScale = 1f - backProgress * 0.15f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = bgAlpha))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { if (scale <= 1f) onDismiss() }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = imageRequest,
                contentDescription = stringResource(R.string.image_hint),
                contentScale = ContentScale.Fit,
                filterQuality = FilterQuality.High,
                modifier = Modifier
                    .fillMaxSize()
                    // 外层 graphicsLayer：退出动画整体缩放（中心点）
                    .graphicsLayer {
                        scaleX = exitScale
                        scaleY = exitScale
                    }
                    // 内层 zoomable：用户双指缩放 / 平移
                    .zoomable(zoomState),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator(modifier = Modifier.size(48.dp))
                    }
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
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