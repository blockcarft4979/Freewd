package com.freewdcmkt.bck.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R

@Composable
fun LoadErrorUiLayout(
    onClick: () -> Unit,
    msg: String? = null,
    buttonMsg: String? = null,
    icon: Painter? = null
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(15.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (msg == null) Text(stringResource(R.string.load_error_hint)) else Text(msg)
            if (buttonMsg != null) {
                Button(onClick = { onClick() }) {
                    if (icon != null) Icon(
                        modifier = Modifier.size(24.dp),
                        painter = icon,
                        contentDescription = null
                    )
                    Text(buttonMsg)
                }
            }
        }

    }
}

@Composable
@Preview(showBackground = true, showSystemUi = true)
private fun Show() {
    LoadErrorUiLayout({}, buttonMsg = "retry")
}