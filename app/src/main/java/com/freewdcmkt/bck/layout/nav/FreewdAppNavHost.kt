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
import com.freewdcmkt.bck.layout.ui.BrowserLayout
import com.freewdcmkt.bck.layout.ui.FeedDetailLayout
import com.freewdcmkt.bck.layout.ui.FeedLayout
import com.freewdcmkt.bck.layout.ui.Notification
import com.freewdcmkt.bck.layout.ui.PostFeedLayout

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
                onToNotification = { navController.navigate(NotificationScreen) }
            )
        }
        composable<FeedScreenData> { backStack ->
            val args = backStack.toRoute<FeedScreenData>()
            FeedLayout(
                zone = args.zone,
                onToFeedDetail = { id, zone ->
                    navController.navigate(FeedDetailScreenData(id, zone))
                },
                onToPostFeed = { id, zone ->
                    navController.navigate(PostFeedScreen(id, zone))
                },
                onBack = { navController.popBackStack() })
        }
        composable<FeedDetailScreenData> { backStack ->
            val args = backStack.toRoute<FeedDetailScreenData>()
            FeedDetailLayout(
                args.id,
                onDeleteFeed = { navController.popBackStack() },
                onBack = { navController.popBackStack() })
        }
        composable<BrowserScreenData> { backSTack ->
            val args = backSTack.toRoute<BrowserScreenData>()
            BrowserLayout(args.url)
        }
        composable<PostFeedScreen> { backStack ->
            val args = backStack.toRoute<PostFeedScreen>()
            PostFeedLayout(
                args.zone,
                onUploaded = { navController.popBackStack() },
                onBack = { navController.popBackStack() })
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
    }
}