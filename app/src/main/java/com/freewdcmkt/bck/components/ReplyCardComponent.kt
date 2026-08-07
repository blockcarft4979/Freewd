package com.freewdcmkt.bck.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.freewdcmkt.bck.R
import com.freewdcmkt.bck.components.freewd.ContentText

@Composable
fun ReplyInputBar(
    username: String,
    onSend: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf("") }
    val isSendEnabled = text.isNotBlank()

    Surface(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .weight(1f),
                maxLines = 5,
                label =  { Text(stringResource(R.string.reply_to_user_hint, username), maxLines = 1) },
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    if (isSendEnabled) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = isSendEnabled,
                shape = CircleShape,
            ) {
                Icon(
                    painterResource(R.drawable.baseline_send_24),
                    stringResource(R.string.send_hint),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}