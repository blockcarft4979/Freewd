package com.freewdcmkt.bck.layout.ui.other

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.sp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.FreewdSwitch
import com.freewdcmkt.bck.util.dynamicColorFlow
import com.freewdcmkt.bck.util.pureBlackTheme
import com.freewdcmkt.bck.util.setDynamicColor
import com.freewdcmkt.bck.util.setPureBlackTheme
import kotlinx.coroutines.launch

@Composable
fun Settings() {
    val context = LocalContext.current
    val enabledDynamicColors by context.dynamicColorFlow().collectAsState(initial = false)
    val enabledPureBlackTheme by context.pureBlackTheme().collectAsState(initial = false)
    val scope = rememberCoroutineScope()
    val dynamicColorSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_hint)) }) }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 15.dp)
        ) {
            Text(
                fontSize = 12.sp,
                text = stringResource(R.string.setting_theme_hint),
                color = MaterialTheme.colorScheme.primary
            )
            FreewdSwitch(
                stringResource(R.string.setting_enable_dynamic_hint),
                checked = enabledDynamicColors && dynamicColorSupported,
                onCheckedChange = { scope.launch { context.setDynamicColor(it) } },
                modifier = Modifier.fillMaxWidth(),
                enabled = dynamicColorSupported
            )
            FreewdSwitch(
                stringResource(R.string.setting_black_theme_hint),
                checked = enabledPureBlackTheme,
                onCheckedChange = { scope.launch { context.setPureBlackTheme(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
