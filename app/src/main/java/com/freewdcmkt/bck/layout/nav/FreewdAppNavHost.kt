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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.freewdcmkt.bck.data.screen.AboutAppScreenData
import com.freewdcmkt.bck.data.screen.BrowserScreenData
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.HomeScreenData
import com.freewdcmkt.bck.data.screen.NotificationScreen
import com.freewdcmkt.bck.data.screen.PostFeedScreen
import com.freewdcmkt.bck.layout.ui.community.FeedDetailLayout
import com.freewdcmkt.bck.layout.ui.community.PostFeedLayout
import com.freewdcmkt.bck.layout.ui.other.About
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
                HomeNavHost(
                    onToFeedDetail = { backStack.add(FeedDetailScreenData(it)) },
                    onToPostFeed = { backStack.add(PostFeedScreen()) },
                    onToNotification = { backStack.add(NotificationScreen) },
                    onToAboutApp = {backStack.add(AboutAppScreenData)}
                )
            }

            entry<FeedDetailScreenData> { args ->
                FeedDetailLayout(
                    args.id,
                    onDeleteFeed = {
                        refreshViewModel.feedRefresh = true
                        backStack.removeLastOrNull()
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<BrowserScreenData> { args ->
                BrowserLayout(args.url)
            }

            entry<PostFeedScreen> {
                PostFeedLayout(
                    onUploaded = {
                        refreshViewModel.feedRefresh = true
                        backStack.removeLastOrNull()
                    },
                    onBack = { backStack.removeLastOrNull() }
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

            entry<AboutAppScreenData> { About() }
        }
    )
}