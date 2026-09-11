package com.freewdcmkt.bck.layout.nav

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.freewdcmkt.bck.data.screen.BrowserScreenData
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.FeedScreenData
import com.freewdcmkt.bck.data.screen.HomeScreenData
import com.freewdcmkt.bck.data.screen.NotificationScreen
import com.freewdcmkt.bck.data.screen.PostFeedScreen
import com.freewdcmkt.bck.data.screen.PreviewImgScreenData
import com.freewdcmkt.bck.data.values.StringValues.REFRESH
import com.freewdcmkt.bck.layout.ui.community.FeedDetailLayout
import com.freewdcmkt.bck.layout.ui.community.FeedLayout
import com.freewdcmkt.bck.layout.ui.community.PostFeedLayout
import com.freewdcmkt.bck.layout.ui.community.PreviewImgUi
import com.freewdcmkt.bck.layout.ui.other.BrowserLayout
import com.freewdcmkt.bck.layout.ui.user.Notification



// 一个简单的共享状态，替代原来的 savedStateHandle[REFRESH]
class RefreshStateViewModel : ViewModel() {
    var feedRefresh by mutableStateOf(false)
    var notificationRefresh by mutableStateOf(false)
}
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FreewdAppNavHost(
    backStack: NavBackStack<NavKey>,
    refreshViewModel: RefreshStateViewModel = viewModel()
) {
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        modifier = Modifier.background(MaterialTheme.colorScheme.background),

        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350, delayMillis = 50)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    )
        },
        popTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) + fadeIn(animationSpec = tween(350, delayMillis = 50)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    )
        },
        predictivePopTransitionSpec = {
            slideInHorizontally(
                initialOffsetX = { -it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { it },
                        animationSpec = tween(350, easing = FastOutSlowInEasing)
                    )
        },

        entryProvider = entryProvider {
            entry<HomeScreenData> {
                HomeNavHost (
                    onToFeed = { zone -> backStack.add(FeedScreenData(zone)) },
                    onToBrowser = { url -> backStack.add(BrowserScreenData(url)) },
                    onToNotification = { backStack.add(NotificationScreen) },
                )
            }

            entry<FeedScreenData> { args ->
                // args 直接就是 FeedScreenData，不需要 toRoute
                val isRefresh = refreshViewModel.feedRefresh
                LaunchedEffect(isRefresh) {
                    if (isRefresh) refreshViewModel.feedRefresh = false
                }
                FeedLayout(
                    zone = args.zone,
                    onToFeedDetail = { id, zone ->
                        backStack.add(FeedDetailScreenData(id, zone))
                    },
                    onToPostFeed = { id, zone ->
                        backStack.add(PostFeedScreen(id, zone))
                    },
                    onBack = { backStack.removeLastOrNull() },
                    onToPreviewImg = { url -> backStack.add(PreviewImgScreenData(url)) },
                    isRefresh = isRefresh
                )
            }

            entry<FeedDetailScreenData> { args ->
                FeedDetailLayout(
                    args.id,
                    onDeleteFeed = {
                        refreshViewModel.feedRefresh = true
                        backStack.removeLastOrNull()
                    },
                    onBack = { backStack.removeLastOrNull() },
                    onToPreviewImg = { url -> backStack.add(PreviewImgScreenData(url)) }
                )
            }

            entry<BrowserScreenData> { args ->
                BrowserLayout(args.url)
            }

            entry<PostFeedScreen> { args ->
                PostFeedLayout(
                    args.zone,
                    onUploaded = {
                        refreshViewModel.feedRefresh = true
                        backStack.removeLastOrNull()
                    },
                    onBack = { backStack.removeLastOrNull() },
                    onToPreviewImg = { url -> backStack.add(PreviewImgScreenData(url)) },
                )
            }

            entry<NotificationScreen> {
                Notification(
                    onToFeedDetail = { id ->
                        backStack.add(FeedDetailScreenData(id = id))
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<PreviewImgScreenData> { args ->
                PreviewImgUi(args.url)
            }
        }
    )
}