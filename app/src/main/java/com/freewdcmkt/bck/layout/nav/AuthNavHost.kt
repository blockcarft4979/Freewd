package com.freewdcmkt.bck.layout.nav

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import com.freewdcmkt.bck.data.screen.ChangePasswordScreenData
import com.freewdcmkt.bck.data.screen.DocumentScreenData
import com.freewdcmkt.bck.data.screen.LoginScreenData
import com.freewdcmkt.bck.data.screen.RegisterScreenData
import com.freewdcmkt.bck.layout.ui.auth.ChangePasswordHost
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
                    onToUserAgreement = { backStack.add(DocumentScreenData(it)) },
                    onChangePassword = { backStack.add(ChangePasswordScreenData) },
                )
            }
            entry<ChangePasswordScreenData> {
                ChangePasswordHost(
                    onChangedPassword = { backStack.removeLastOrNull() }
                )
            }
            entry<RegisterScreenData> {
                RegisterLayout(
                    onToUserAgreement = { backStack.add(DocumentScreenData(it)) },
                )
            }
            entry<DocumentScreenData> { aboutScreenData ->
                Document(
                    onBack = { backStack.removeLastOrNull() },
                    aboutScreenData.url
                )
            }
        },
    )

}