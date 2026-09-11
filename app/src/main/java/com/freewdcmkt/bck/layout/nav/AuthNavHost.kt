package com.freewdcmkt.bck.layout.nav

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.freewdcmkt.bck.data.screen.AboutScreenData
import com.freewdcmkt.bck.data.screen.LoginScreenData
import com.freewdcmkt.bck.data.screen.RegisterScreenData
import com.freewdcmkt.bck.layout.ui.auth.LoginLayout
import com.freewdcmkt.bck.layout.ui.auth.RegisterLayout
import com.freewdcmkt.bck.layout.ui.other.Document
import com.freewdcmkt.bck.viewmodel.auth.LogInViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AuthLayout(viewModel: LogInViewModel = viewModel()) {

    val backStack = remember { mutableStateListOf<NavKey>(LoginScreenData) }

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        transitionSpec = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = tween(350, easing = FastOutSlowInEasing)
            ) togetherWith
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
            entry<LoginScreenData> {
                LoginLayout(
                    onRegister = { backStack.add(RegisterScreenData) },
                    onLogin = { account, password ->
                        if (account.isNotEmpty() && password.isNotEmpty()) run {
                            viewModel.fetchData(
                                password,
                                account
                            )
                        }
                    },
                    onToUserAgreement = { backStack.add(AboutScreenData(it)) },
                    onToPolicyPrivacy = { backStack.add(AboutScreenData(it)) }
                )
            }
            entry<RegisterScreenData> {
                RegisterLayout(
                    onToUserAgreement = { backStack.add(AboutScreenData(it)) },
                    onToPolicyPrivacy = { backStack.add(AboutScreenData(it)) }
                )
            }
            entry<AboutScreenData> { aboutScreenData ->
                Document(
                    onBack = { backStack.removeLastOrNull() },
                    aboutScreenData.url
                )
            }
        },
    )

}