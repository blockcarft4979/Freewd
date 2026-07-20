package com.freewdcmkt.bck.layout

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.freewdcmkt.bck.data.screen.BrowserScreenData
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.FeedScreenData
import com.freewdcmkt.bck.data.screen.HomeScreenData

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FreewdAppNavLayout() {
    val navController = rememberNavController()
    NavHost(
        modifier = Modifier.background(MaterialTheme.colorScheme.background),
        navController = navController,
        startDestination = HomeScreenData,
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350, delayMillis = 50))
        },
        // 返回时的退出动画（当前页滑出到右侧）
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        },
        // 正向导航的进入动画（新页面从右侧滑入）
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350, delayMillis = 50))
        },
        // 正向导航的退出动画（旧页面滑出到左侧）
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 2 },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable<HomeScreenData> {
            HomeLayout(
                onToFeed = { zone ->
                    navController.navigate(FeedScreenData(zone))
                },
                onToBrowser = { url -> navController.navigate(BrowserScreenData(url)) }
            )
        }
        composable<FeedScreenData> { backStack ->
            val args = backStack.toRoute<FeedScreenData>()
            FeedLayout(
                zone = args.zone,
                onToFeedDetail = { id ->
                    navController.navigate(FeedDetailScreenData(id))
                })
        }
        composable<FeedDetailScreenData> { backStack ->
            val args = backStack.toRoute<FeedDetailScreenData>()
            FeedDetailLayout(args.id)
        }
        composable<BrowserScreenData> { backSTack ->
            val args = backSTack.toRoute<BrowserScreenData>()
            BrowserLayout(args.url)
        }

    }
}