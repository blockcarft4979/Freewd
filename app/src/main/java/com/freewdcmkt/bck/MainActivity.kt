package com.freewdcmkt.bck

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.freewdcmkt.bck.data.screen.FeedDetailScreenData
import com.freewdcmkt.bck.layout.FreewdAppNavHost
import com.freewdcmkt.bck.layout.LoginLayout
import com.freewdcmkt.bck.ui.theme.FreewdTheme
import com.freewdcmkt.bck.viewmodel.MainViewmodel

class MainActivity : ComponentActivity() {
    private var currentIntent by mutableStateOf<Intent?>(null)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        currentIntent = intent
        setContent {
            FreewdTheme {
                MainLayout(
                    intent = currentIntent?: Intent()
                )
                Log.d("MAIN LAYOUT", currentIntent?.data.toString())
            }
        }
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        currentIntent = intent
    }

}

@Composable
fun MainLayout(viewmodel: MainViewmodel = viewModel(), intent: Intent) {
    val isLogin by viewmodel.isLogin.collectAsState()
    val navController = rememberNavController()

    // 1. 解析 intent，保存参数
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

    when (isLogin) {
        true -> FreewdAppNavHost(navController)
        false -> LoginLayout()
    }

    LaunchedEffect(isLogin) {
        if (isLogin) {
            pendingDeepLink.value?.let { (id, zone) ->
                navController.navigate(FeedDetailScreenData(id, zone))
                pendingDeepLink.value = null
            }
        }
    }
}