package com.freewdcmkt.bck.layout.ui.other

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.FreewdSwitch
import com.freewdcmkt.bck.util.dynamicColorFlow
import com.freewdcmkt.bck.util.setDynamicColor
import kotlinx.coroutines.launch

@Composable
fun Settings() {
    val context = LocalContext.current
    val enabledDynamicColors by context.dynamicColorFlow().collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_hint)) }) }) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(horizontal = 15.dp)) {
            FreewdSwitch(
                stringResource(R.string.setting_enable_dynamic_hint),
                checked = enabledDynamicColors && dynamicColorSupported,
                onCheckedChange = { scope.launch { context.setDynamicColor(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = dynamicColorSupported
            )
        }
    }
}

//@Composable
//private fun SettingCard() {
//    Card(shape = RoundedCornerShape(16.dp)) {
//        Row(modifier = Modifier.padding(10.dp)) {
//            FreewdSwitch()
//        }
//    }
//}
