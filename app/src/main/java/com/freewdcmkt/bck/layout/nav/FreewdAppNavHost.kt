package com.freewdcmkt.bck.layout.nav

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.freewdcmkt.bck.data.screen.AboutAppScreenData
import com.freewdcmkt.bck.data.screen.BrowserScreenData
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.HomeScreenData
import com.freewdcmkt.bck.data.screen.NotificationScreen
import com.freewdcmkt.bck.data.screen.PostFeedScreen
import com.freewdcmkt.bck.data.screen.SearchScreen
import com.freewdcmkt.bck.data.screen.SettingsScreenData
import com.freewdcmkt.bck.data.screen.UserCenterScreen
import com.freewdcmkt.bck.data.screen.UserProfileScreen
import com.freewdcmkt.bck.layout.ui.community.FeedDetailLayout
import com.freewdcmkt.bck.layout.ui.community.PostFeedLayout
import com.freewdcmkt.bck.layout.ui.community.SearchLayout
import com.freewdcmkt.bck.layout.ui.other.About
import com.freewdcmkt.bck.layout.ui.other.BrowserLayout
import com.freewdcmkt.bck.layout.ui.other.Settings
import com.freewdcmkt.bck.layout.ui.user.Notification
import com.freewdcmkt.bck.layout.ui.user.UserProfileLayout


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
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        modifier = Modifier.background(MaterialTheme.colorScheme.background),

        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it / 4 },
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeIn(tween(320)) togetherWith
                    slideOutHorizontally(
                        targetOffsetX = { -it / 4 },           // 旧页只左退 1/4
                        animationSpec = tween(320, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                targetScale = 0.92f,                   // 旧页缩到 0.92，制造"退后一层"
                animationSpec = tween(320, easing = FastOutSlowInEasing)
            ) + fadeOut(tween(320))
        },

        predictivePopTransitionSpec = {
            (scaleIn(initialScale = 0.92f) + fadeIn()) togetherWith
                    (slideOutHorizontally(targetOffsetX = { it / 4 }) +
                            scaleOut(targetScale = 0.92f) +
                            fadeOut())
        },

        popTransitionSpec = {
            (scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            ) + fadeIn(
                animationSpec = tween(300, easing = FastOutSlowInEasing)
            )) togetherWith
                    (slideOutHorizontally(
                        targetOffsetX = { it / 4 },
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + scaleOut(
                        targetScale = 0.92f,
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ) + fadeOut(
                        animationSpec = tween(300, easing = FastOutSlowInEasing)
                    ))
        },
        entryProvider = entryProvider {
            entry<HomeScreenData> {
                HomeNavHost(
                    onToFeedDetail = { backStack.add(FeedDetailScreenData(it)) },
                    onToPostFeed = { backStack.add(PostFeedScreen()) },
                    onToNotification = { backStack.add(NotificationScreen()) },
                    onToSearch = { backStack.add(SearchScreen) },
                    onToAboutApp = { backStack.add(AboutAppScreenData) },
                    onToUserCenter = { backStack.add(UserCenterScreen) },
                    onToSettings = { backStack.add(SettingsScreenData) },
                    onToUserProfile = {backStack.add(UserProfileScreen(it))}
                )
            }

            entry<SearchScreen> {
                SearchLayout(
                    onToFeedDetail = { id -> backStack.add(FeedDetailScreenData(id)) },
                    onToUserProfile = { qq -> backStack.add(UserProfileScreen(qq)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<UserProfileScreen> { args ->
                UserProfileLayout(
                    qq = args.qq,
                    onToFeedDetail = { id -> backStack.add(FeedDetailScreenData(id)) },
                    onBack = { backStack.removeLastOrNull() }
                )
            }

            entry<UserCenterScreen> {
                UserProfileLayout(
                    qq = "",
                    onToFeedDetail = { id -> backStack.add(FeedDetailScreenData(id)) },
                    onBack = { backStack.removeLastOrNull() }
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
                    onToUserProfile = {backStack.add(UserProfileScreen(it))}
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
            entry<SettingsScreenData> { Settings() }

        }
    )
}