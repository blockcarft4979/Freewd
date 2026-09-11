package com.freewdcmkt.bck.layout.main

import android.app.ComponentCaller
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.data.screen.HomeData
import com.freewdcmkt.bck.data.screen.HomeScreenData
import com.freewdcmkt.bck.layout.nav.AuthLayout
import com.freewdcmkt.bck.layout.nav.FreewdAppNavHost
import com.freewdcmkt.bck.ui.theme.FreewdTheme
import com.freewdcmkt.bck.viewmodel.main.LoginState
import com.freewdcmkt.bck.viewmodel.main.MainViewmodel

class MainActivity : ComponentActivity() {
    private val intentState = mutableStateOf<Intent?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.d("MAIN ERROR", e.message.toString())
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        intentState.value = intent

        setContent {
            FreewdTheme {
                MainLayout(
                    intent = intentState.value ?: Intent()
                )

            }
        }
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentState.value = intent
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        onNewIntent(intent)
    }
}

@Composable
fun MainLayout(viewmodel: MainViewmodel = viewModel(), intent: Intent) {
    val loginState by viewmodel.loginState.collectAsState()
    val backStack = rememberNavBackStack(HomeScreenData)

    // 解析 intent，保存参数
    val pendingDeepLink = remember { mutableStateOf<Pair<Int, Int>?>(null) }
    LaunchedEffect(intent) {
        val uri = intent.data
        if (uri?.host == "community.freewd.top" && uri.path == "/u/page") {
            val id = uri.getQueryParameter("id")?.toIntOrNull()
            val zone = uri.getQueryParameter("zone")?.toIntOrNull() ?: 1
            if (id != null) {
                pendingDeepLink.value = id to zone
            }
        }
    }

    when (loginState) {
        is LoginState.Loading -> {}
        is LoginState.LoggedIn -> {
            FreewdAppNavHost(
                backStack = backStack
            )
            LaunchedEffect(pendingDeepLink.value) {
                pendingDeepLink.value?.let { (id, zone) ->
                    backStack.add(FeedDetailScreenData(id, zone))
                    pendingDeepLink.value = null
                }
            }
        }

        is LoginState.LoggedOut -> AuthLayout()
    }

}