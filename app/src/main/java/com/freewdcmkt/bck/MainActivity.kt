package com.freewdcmkt.bck

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.freewdcmkt.bck.layout.FreewdAppNavLayout
import com.freewdcmkt.bck.layout.LoginLayout
import com.freewdcmkt.bck.ui.theme.FreewdTheme
import com.freewdcmkt.bck.viewmodel.MainViewmodel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FreewdTheme {
                MainLayout()
            }
        }
    }
}

@Composable
fun MainLayout(viewmodel: MainViewmodel = viewModel()) {
    val isLogin by viewmodel.isLogin.collectAsState()
    Column() {
        when (isLogin) {
            true -> FreewdAppNavLayout()
            false -> LoginLayout()
        }
    }
}
