package com.freewdcmkt.bck.components.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** M3 小顶栏内容高度，对应 AppBarSmallTokens.ContainerHeight */
val TopAppBarHeight: Dp = 64.dp

/**
 * 会**整体滑出屏幕**的顶栏。
 *
 * 为什么不能直接用 M3 的 `TopAppBar` + `enterAlwaysScrollBehavior`：
 * 它的「收起」是把自身高度缩掉（`layoutHeight = maxLayoutHeight + heightOffset`），
 * 但**标题内容并不会被裁掉**——`contentHeight = layoutHeight + topPadding - bottomPadding`
 * 里的 topPadding 含状态栏内边距，所以它不会归零，标题仍按原位绘制；顶栏背景又是透明的。
 * 结果就是头像 / 用户名 / UID / 铃铛直接压在列表卡片上面。这是「顶栏和卡片重叠」的真正原因。
 *
 * 这里改成：布局占位保持展开高度不变（所以 [feedListTopPadding] 的公式不用动），
 * 只把整条顶栏（含标题）用 `graphicsLayer` 的 `translationY` 平移出屏幕。标题跟着一起走，
 * 永远不会停在卡片上。用 lambda 形式是为了把 [TopAppBarState.heightOffset] 的读取
 * 留在**绘制阶段**，避免滚动时逐帧重组。
 */
@Composable
fun OffscreenTopAppBar(
    topAppBarState: TopAppBarState,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    actions: @Composable () -> Unit = {}
) {
    // 顶栏内容被 windowInsetsPadding 往下推了「状态栏高度」这么多，
    // 所以彻底滑出去需要额外补上这一段
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val topInsetPx = with(LocalDensity.current) { topInset.toPx() }
    val barHeightPx = with(LocalDensity.current) { TopAppBarHeight.toPx() }
    val surface = MaterialTheme.colorScheme.surface
    TopAppBar(
        title = title,
        actions = { actions() },
        scrollBehavior = scrollBehavior,
        // 底色用「同色 → 透明」的竖直渐变，而不是纯透明：
        // 收起过程中卡片会从顶栏底边滑出来，有底色才不会让标题和卡片文字叠在一起；
        // 渐变到透明又保留了「内容从顶栏底下钻出来」的观感，也不会出现硬边。
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = Color.Transparent
        ),
        modifier = modifier
            .drawWithContent {
                drawRect(
                    brush = Brush.verticalGradient(0f to surface, 1f to Color.Transparent)
                )
                drawContent()
            }
            .graphicsLayer {
                // 用 graphicsLayer 的 translationY 把整条顶栏（含标题）移出屏幕：
                // 这是 Compose 里标准的「移出视野」方式，不受布局槽位裁剪影响；
                // 而 offset 只改绘制位置、布局占位仍在槽位里，超出槽位的部分
                // 会被父节点裁掉，行为依赖具体实现，很脆。
                //
                // 位移夹在 [0, 状态栏 + 64dp] 内：heightOffset 的合法范围是
                // [-(状态栏+64dp), 0]，一旦这个值被污染，Float -> Int 的取模转换
                // 会把顶栏丢到完全离谱的位置（截图里顶栏内容跑进状态栏那种）。
                val maxShiftPx = barHeightPx + topInsetPx
                val collapsed = (-topAppBarState.heightOffset).coerceIn(0f, maxShiftPx)
                translationY = -collapsed
            }
    )
}

/**
 * 帖子列表的上内边距 = 当前顶栏底边的 y 坐标。
 *
 * 配合 [OffscreenTopAppBar] 使用：顶栏布局高度恒为「状态栏 + 64dp」，
 * 只是被平移上去了，所以内边距跟着收起量一起减；完全收起时 = 0，
 * 内容一路铺到屏幕最上面。
 */
@Composable
fun feedListTopPadding(topAppBarState: TopAppBarState, density: Density): Dp =
    feedListTopPadding(
        statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
        topAppBarState = topAppBarState,
        density = density
    )

/**
 * 同上，但状态栏高度由调用方传入。
 * 供需要在**布局阶段**（而不是 composition 阶段）计算内边距的场景使用：
 * 后者先在 composition 阶段取好状态栏高度，避免在 measure 里读 WindowInsets。
 */
fun feedListTopPadding(
    statusBarHeight: Dp,
    topAppBarState: TopAppBarState,
    density: Density
): Dp {
    val fullyExpanded = statusBarHeight + TopAppBarHeight
    val collapsed = with(density) { (-topAppBarState.heightOffset).toDp() }
    return (fullyExpanded - collapsed).coerceAtLeast(0.dp)
}

/** 渐进模糊的淡化剖面：`stops` 是 (y, 该处保留的不透明度)。 */
@Immutable
data class FadeProfile(val stops: List<Pair<Dp, Float>>)

/**
 * 渐进模糊的一层：模糊半径 + 它在淡化剖面上负责的那一段（[start, end]）。
 * 该层的遮罩 = 剖面曲线的增量（见 [ProgressiveBlurScrim]），保证各层首尾相接、
 * 叠加后正好还原出一条连续的淡化曲线。
 */
@Immutable
data class BlurLayer(val radius: Dp, val start: Dp, val end: Dp)

/**
 * 状态栏区域的**渐进式模糊（progressive blur）**底衬。
 *
 * 两个前提必须说清楚：
 * 1. `Modifier.blur` 只模糊**自己**的内容（内部就是给自身套一个
 *    `graphicsLayer { renderEffect = BlurEffect(...); clip = true }`），它采样不到
 *    兄弟节点画在背后的像素。所以要「模糊背后的内容」只能把内容按同一滚动位置
 *    再画一份进来——这就是需要 [content] 槽的原因。
 * 2. 一个 `graphicsLayer` 只有一个 `renderEffect`，做不到「半径随 y 递减」。
 *    真正的渐进模糊只能叠：[layers] 个副本，半径递增、各自负责剖面的一段。
 *
 * 各层内容都从 [topInset] 开始画（不是从 0），层内再用 `offset(y = -topInset)`
 * 平移回去，从而与主列表像素级对齐。
 *
 * @param content 需要被模糊的「背景」，会被调用 [layers].size 次。调用方必须为每一层
 *   准备**各自独立**的滚动状态，并与主列表逐帧同步
 */
@Composable
fun ProgressiveBlurScrim(
    topAppBarState: TopAppBarState,
    density: Density,
    layers: List<BlurLayer> = DefaultBlurLayers,
    profile: FadeProfile = DefaultFadeProfile,
    content: @Composable (layerIndex: Int) -> Unit
) {
    val topInset = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // 顶栏滑出过程中内容并不会进入这条带子（列表上内边距始终贴着顶栏底边，
    // 顶栏在 64dp 处就停住了）；只有继续上滑、内容真正开始进入状态栏时才需要底衬。
    val contentUnderStatusBar = with(density) {
        -topAppBarState.heightOffset >= TopAppBarHeight.toPx() - 0.5f
    }
    if (!contentUnderStatusBar) return

    val bandHeight = topInset + ScrimBandHeight

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(bandHeight)
    ) {
        layers.forEachIndexed { index, layer ->
            val bandPx = with(density) { bandHeight.toPx() }
            val startPx = with(density) { layer.start.toPx() }
            val endPx = with(density) { layer.end.toPx() }
            val stopsPx = profile.stops.map { (y, alpha) -> with(density) { y.toPx() } to alpha }

            // 整层先模糊（graphicsLayer + BlurEffect），再在层外叠渐变遮罩。
            // 遮罩必须画在层外，否则它会连自己一起被模糊。
            // Offscreen 让 DstIn 只作用于这一层自己的内容。
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bandHeight)
                    .blur(layer.radius, edgeTreatment = BlurredEdgeTreatment.Rectangle)
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    .drawWithContent {
                        drawContent()
                        // 该层负责的区间 = 淡化曲线在 [start, end] 上的增量。
                        // 用增量而不是绝对值：DstIn 是「擦除」语义，若每层都画 1→0 的遮罩，
                        // 重叠区间会互相擦除出凹坑，接缝就看出来了。
                        val alphaFrom = cumulativeFade(stopsPx, startPx)
                        val alphaTo = cumulativeFade(
                            stopsPx,
                            if (endPx <= 0f) bandPx else endPx
                        )
                        val height = (endPx - startPx).coerceAtLeast(0f)
                        if (height <= 0f) return@drawWithContent
                        drawRect(
                            brush = Brush.verticalGradient(
                                0f to Color.Black.copy(alpha = alphaFrom),
                                1f to Color.Black.copy(alpha = alphaTo),
                                startY = startPx,
                                endY = startPx + height
                            ),
                            topLeft = Offset(0f, startPx),
                            size = Size(size.width, height),
                            blendMode = BlendMode.DstIn
                        )
                    }
            ) {
                Box(
                    // 各层内容都从 topInset 开始画，这里平移回去与主列表像素级对齐
                    // （offset 在布局阶段作用，不触发重组）
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(bandHeight)
                        .offset(y = -topInset)
                ) {
                    content(index)
                }
            }
        }
    }
}

/** 剖面在 [y] 处已经累积擦除的不透明度（0 = 完全保留，1 = 完全擦掉）。 */
private fun cumulativeFade(stops: List<Pair<Float, Float>>, y: Float): Float {
    if (stops.isEmpty()) return 0f
    if (y <= stops.first().first) return 1f - stops.first().second
    for (i in 0 until stops.size - 1) {
        val (y0, a0) = stops[i]
        val (y1, a1) = stops[i + 1]
        if (y <= y1) {
            val t = if (y1 - y0 <= 0f) 1f else (y - y0) / (y1 - y0)
            val alpha = a0 + (a1 - a0) * t
            return 1f - alpha
        }
    }
    return 1f - stops.last().second
}

/** 底衬总高度（不含状态栏）：给渐进过渡留够空间 */
private val ScrimBandHeight = 40.dp

/**
 * 淡化剖面：y=0 处完全不透明，往下到 56dp 处彻底消失。
 * 曲线是「快-慢-快」的手感，避免线性渐变那种一眼看出分段的感觉。
 */
private val DefaultFadeProfile = FadeProfile(
    stops = listOf(
        0.dp to 1.00f,
        14.dp to 0.85f,
        28.dp to 0.55f,
        42.dp to 0.22f,
        56.dp to 0.00f
    )
)

/** 三层递进，半径越靠上越大；每段对应 [DefaultFadeProfile] 的一个区间。 */
private val DefaultBlurLayers = listOf(
    BlurLayer(radius = 24.dp, start = 0.dp, end = 14.dp),
    BlurLayer(radius = 12.dp, start = 14.dp, end = 28.dp),
    BlurLayer(radius = 6.dp, start = 28.dp, end = 56.dp)
)
