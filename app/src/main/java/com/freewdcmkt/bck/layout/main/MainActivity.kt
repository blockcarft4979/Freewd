package com.freewdcmkt.bck.layout.main

import android.app.Application
import android.app.ComponentCaller
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation3.runtime.rememberNavBackStack
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.HomeScreenData
import com.freewdcmkt.bck.layout.nav.AuthLayout
import com.freewdcmkt.bck.layout.nav.FreewdAppNavHost
import com.freewdcmkt.bck.ui.theme.FreewdTheme
import com.freewdcmkt.bck.util.dynamicColorFlow
import com.freewdcmkt.bck.viewmodel.main.LoginState
import com.freewdcmkt.bck.viewmodel.main.MainViewmodel
import androidx.core.net.toUri

class MainActivity : ComponentActivity() {

    private val dynamicIntentUrl = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.d("MAIN ERROR", e.message.toString())
        }
        super.onCreate(savedInstanceState)

        // 状态栏透明 scrim，避免 API 35+ 灰色保护层
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        // 只在首次创建时消费 intent；重建（savedInstanceState 非 null）时不处理
        if (savedInstanceState == null) {
            dynamicIntentUrl.value = intent.data?.toString()
        }

        setContent {
            val dynamicColors by dynamicColorFlow().collectAsState(initial = false)

            FreewdTheme(dynamicColor = dynamicColors) {
                MainLayout(
                    deepLinkUrl = dynamicIntentUrl.value,
                    onDeepLinkConsumed = { dynamicIntentUrl.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        dynamicIntentUrl.value = intent.data?.toString()
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        onNewIntent(intent)
    }
}

@Composable
fun MainLayout(
    deepLinkUrl: String?,
    onDeepLinkConsumed: () -> Unit
) {
    val context = LocalContext.current
    val viewmodel: MainViewmodel = viewModel(
        factory = viewModelFactory {
            initializer {
                MainViewmodel(context.applicationContext as Application)
            }
        }
    )

    val loginState by viewmodel.loginState.collectAsState()
    val backStack = rememberNavBackStack(HomeScreenData)

    val pendingDeepLink = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(deepLinkUrl) {
        if (deepLinkUrl == null) return@LaunchedEffect
        val uri = deepLinkUrl.toUri()
        if (uri.host == "community.freewd.top" && uri.path == "/u/page") {
            uri.getQueryParameter("id")
                ?.toIntOrNull()
                ?.let { pendingDeepLink.value = it }
        }
        onDeepLinkConsumed()
    }

    when (loginState) {
        is LoginState.Loading -> {}

        is LoginState.LoggedIn -> {
            FreewdAppNavHost(backStack = backStack)

            LaunchedEffect(pendingDeepLink.value) {
                pendingDeepLink.value?.let { id ->
                    backStack.add(FeedDetailScreenData(id))
                    pendingDeepLink.value = null
                }
            }
        }

        is LoginState.LoggedOut -> AuthLayout()
    }
}