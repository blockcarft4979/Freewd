package com.freewdcmkt.bck.layout.nav

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.freewdcmkt.bck.data.screen.BrowserScreenData
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.FeedScreenData
import com.freewdcmkt.bck.data.screen.HomeScreenData
import com.freewdcmkt.bck.data.screen.NotificationScreen
import com.freewdcmkt.bck.data.screen.PostFeedScreen
import com.freewdcmkt.bck.data.screen.PreviewImgScreenData
import com.freewdcmkt.bck.data.screen.UserCenterScreenData
import com.freewdcmkt.bck.data.values.StringValues.REFRESH
import com.freewdcmkt.bck.layout.ui.other.BrowserLayout
import com.freewdcmkt.bck.layout.ui.community.PreviewImgUi
import com.freewdcmkt.bck.layout.ui.community.FeedDetailLayout
import com.freewdcmkt.bck.layout.ui.community.FeedLayout
import com.freewdcmkt.bck.layout.ui.community.PostFeedLayout
import com.freewdcmkt.bck.layout.ui.user.Notification


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FreewdAppNavHost(navController: NavHostController) {

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
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        },
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350, delayMillis = 50))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            )
        }
    ) {
        composable<HomeScreenData> {
            HomeLayout(
                onToFeed = { zone ->
                    navController.navigate(FeedScreenData(zone))
                },
                onToBrowser = { url -> navController.navigate(BrowserScreenData(url)) },
                onToNotification = { navController.navigate(NotificationScreen) },
                onToUserCenter = { navController.navigate(UserCenterScreenData(0)) }
            )
        }
        composable<FeedScreenData> { backStack ->
            val args = backStack.toRoute<FeedScreenData>()
            val savedStateHandle = backStack.savedStateHandle
            val isRefresh by savedStateHandle.getStateFlow(REFRESH, false).collectAsState()
            LaunchedEffect(isRefresh) { savedStateHandle[REFRESH] = false }
            FeedLayout(
                zone = args.zone,
                onToFeedDetail = { id, zone ->
                    navController.navigate(FeedDetailScreenData(id, zone))
                },
                onToPostFeed = { id, zone ->
                    navController.navigate(PostFeedScreen(id, zone))
                },
                onBack = { navController.popBackStack() },
                onToPreviewImg = { url -> navController.navigate(PreviewImgScreenData(url)) },
                isRefresh = isRefresh
            )
        }
        composable<FeedDetailScreenData> { backStack ->
            val args = backStack.toRoute<FeedDetailScreenData>()
            FeedDetailLayout(
                args.id,
                onDeleteFeed = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(REFRESH, true)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                onToPreviewImg = { url -> navController.navigate(PreviewImgScreenData(url)) })
        }
        composable<BrowserScreenData> { backSTack ->
            val args = backSTack.toRoute<BrowserScreenData>()
            BrowserLayout(args.url)
        }
        composable<PostFeedScreen> { backStack ->
            val args = backStack.toRoute<PostFeedScreen>()

            PostFeedLayout(
                args.zone,
                onUploaded = {
                    navController.previousBackStackEntry?.savedStateHandle?.set(REFRESH, true)
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() },
                onToPreviewImg = { url -> navController.navigate(PreviewImgScreenData(url)) },
            )
        }
        composable<NotificationScreen> {
            Notification(
                onToFeedDetail = { id ->
                    navController.navigate(
                        FeedDetailScreenData(
                            id = id
                        )
                    )
                }, onBack = { navController.popBackStack() }
            )
        }
        composable<PreviewImgScreenData> { backStack ->
            val args = backStack.toRoute<PreviewImgScreenData>()
            PreviewImgUi(args.url)
        }

    }
}