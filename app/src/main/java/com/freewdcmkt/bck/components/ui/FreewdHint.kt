package com.freewdcmkt.bck.components.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@Composable
fun FreewdHint(icon: Int? = null, hint: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 15.dp)
    ) {
        Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            if (icon != null) Icon(
                painter = painterResource(icon),
                null,
                modifier = Modifier.size(48.dp)
            )
            Text(hint)
        }
    }
}

@Composable
@Preview(showBackground = true)
private fun Show() {
    FreewdHint(
        R.drawable.baseline_notifications_none_24,
        stringResource(R.string.notification_hint)
    )
}